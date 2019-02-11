#include "statement.h"

using skiff::compilation_types::compilation_scope;
using skiff::compilation_types::compiled_skiff;

namespace skiff
{
    namespace statements
    {
        compiled_skiff statement::compile(compilation_scope *env)
        {
            return {raw};
        }

        compiled_skiff value::compile(compilation_scope *env)
        {
            if(val.get_type() == tokenizer::literal_type::STRING)
            {
                compiled_skiff cs = compiled_skiff("\"" + val.get_value() + "\"", type_statement("String")
                        .with_custom_c_len(val.get_value().size() + 1));
                cs.requires_allocation = true;
                return cs;
            }

            return compiled_skiff(val.get_value(), type_statement("Int"));

        }

        compiled_skiff variable::compile(compilation_scope *env)
        {
            compilation_scope::c_var vr = env->get_variable(name);
            return {"*((" + vr.typ.get_c_type() + " *)(stack + " + std::to_string(vr.offset) + "))", vr.typ};
//            if(ts.is_ref_type())
//            {
//                return {"(" + ts.get_c_type() + ") *((size_t *) stack + " + std::to_string(
//                        env->get_variable(name).offset) + ")", ts};
//            }
//            return {name, ts};
        }

        compiled_skiff assignment::compile(compilation_scope *env)
        {
            compiled_skiff value = this->val->compile(env);
            string name = this->name->compile(env).get_line();
            if(value.type.is_ref_type())
            {
//                compilation_types::variable_table * sm = env->get_stack_manager();
//                string offset = std::to_string(sm->get_var(name));
//                string type_name = value.type.compile(env).get_line();
//                vector<string> compiled_lines = {
//                        "*((size_t *) (stack + " + offset + ")) = new(" +
//                        std::to_string(value.type.get_c_len()) + ")",
//                        // (uint8_t *) *((size_t *) (stack + 0))
//                        "memcpy(((" + type_name + ") *((size_t *) stack + " + offset + "))," + value.get_line() + "," + std::to_string(value.type.get_c_len()) + ")"
//                };

//                return compiled_skiff(compiled_lines);
            }
            return {name + " = " + value.get_line(), value.type};
        }

        compiled_skiff declaration_with_assignment::compile(compilation_scope *env)
        {
            compilation_scope::c_var vr = env->define_variable(name, type.get_name());
            string var_ref = "*((" + vr.typ.get_c_type() + " *)(stack + " + std::to_string(vr.offset) + "))";
            if(type.is_ref_type())
            {
                compiled_skiff cs = value->compile(env);
                string offset = std::to_string(vr.offset);
                vector<string> compiled_lines = {
                        var_ref + " = (" + vr.typ.get_c_type() + ") new(" + std::to_string(cs.type.get_c_len()) + ");",

                        "memcpy(" + var_ref + "," + cs.get_line() + "," + std::to_string(cs.type.get_c_len()) + ");"
                };

                return compiled_skiff(compiled_lines);
            }

            return {var_ref + " = " + value->compile(env).get_line()};
        }

        compiled_skiff type_statement::compile(compilation_scope *env)
        {
            return {this->get_c_type()};
        }

        std::string type_statement::get_c_symbol()
        {
            if(name == "Int")
            {
                return "%i";
            }
            else if(name == "String")
            {
                return "%s";
            }
            else
            {
                return "%s";
            }
        }

        size_t type_statement::get_c_len() const
        {
            if(has_custom_len)
            {
                return custom_len;
            }
            if(name == "Int")
            {
                return 4;
            }
            else if(name == "String")
            {
                return 8; // pointer size on 64 bit systems with gcc (needs to be changed for portability)
            }
            else if(name == "None")
            {
                return 0;
            }
            else if(this->is_ref_type())
            {
                return 8;
            }
            return 0;
        }

        bool type_statement::is_ref_type() const
        {
            return !(this->name == "Int" || this->name == "Char");
        }

        type_statement type_statement::with_custom_c_len(size_t len)
        {
            this->has_custom_len = true;
            this->custom_len = len;
            return *this;
        }

        string type_statement::get_c_type()
        {
            if(name == "Int")
            {
                return "int32_t";
            }
            else if(name == "String")
            {
                return "uint8_t *";
            }
            else if(name == "None")
            {
                return "void";
            }
            return name;
        }

        compiled_skiff function_call::compile(compilation_scope *env)
        {
            vector<string> compile_params;
            string compiled_name = name.compile(env).get_line();
            if(compiled_name == "print")
            {
                compiled_name = "printf";
                env->add_include("stdio.h", false);
                vector<string>  format;
                for(statement *s : params)
                {
                    compiled_skiff p = s->compile(env);
                    compile_params.push_back(p.get_line());
                    format.push_back(p.type.get_c_symbol());
                }
                compile_params.insert(compile_params.begin(), "\"" + utils::join(format, " ") + "\\n\"");
                return compiled_skiff(compiled_name + "(" + utils::join(compile_params, ",") + ")", type_statement("Int"));
            }

            compilation_scope::c_function comp_func = env->get_function(compiled_name);

            for(statement *s : params)
            {
                compile_params.push_back(s->compile(env).get_line());
            }
            return {comp_func.compiled_name + "(" + utils::join(compile_params, ",") + ")", comp_func.return_type};
        }

        compiled_skiff function_definition::compile(compilation_types::compilation_scope *env)
        {
            vector<string> params_named;
            vector<string> params_typed;

            auto * scoped_env = new compilation_types::compilation_scope(env, true);

            for(function_parameter fp : this->params)
            {
                params_named.push_back(fp.typ.compile(env).get_line() + " " + fp.name);
                params_typed.push_back(fp.typ.compile(env).get_line());
                scoped_env->define_variable(fp.name, fp.typ);
            }
            if(this->params.empty())
            {
                params_named.push_back("void");
                params_typed.push_back("void");
            }
            string comp_name = env->get_prefix() + this->name + "_" + env->get_running_id();
            string return_comp = this->returns.compile(env).get_line();
            string sig = return_comp + " " +
                    comp_name + " " +
                    "(" + utils::join(params_named, ",") + ")";

            vector<string> body;
            for(statement * s : this->body)
            {
                compiled_skiff compiled_line = s->compile(scoped_env);
                if(compiled_line.content.size() == 1)
                {
                    body.push_back("\t" + compiled_line.get_line() + ";");
                }
                else
                {
                    for(string line : compiled_line.content)
                    {
                        body.push_back("\t" + line);
                    }
                }
            }
            auto pass_through_commits = scoped_env->commit_marked_vars();

            for(string c : pass_through_commits.preamble)
            {
                body.insert(body.begin(), "\t" + c + ";");
            }

            env->declare_function(this->name, comp_name, sig, body, this->returns);

            return compiled_skiff(pass_through_commits.commits);//{return_comp + " (*" + this->name + ")(" + utils::join(params_typed, ",") + ") = &" + comp_name};
        }

        compiled_skiff math_statement::compile(compilation_types::compilation_scope *env)
        {
            string o = this->string_math_op();
            compiled_skiff compiledSkiff = this->statement1->compile(env);
            return {"(" + compiledSkiff.get_line() + " " + o + " " + this->statement2->compile(env).get_line() + ")", compiledSkiff.type};
        }

        std::string math_statement::string_math_op()
        {
           switch(this->opr)
           {
               case op::ADD: return "+";
               case op::SUB: return "-";
               case op::MUL: return "*";
               case op::DIV: return "/";
               case op::EXP: return "**";
           }
        }

        compiled_skiff return_statement::compile(compilation_types::compilation_scope *env)
        {
            return {"return " + this->returns->compile(env).get_line()};
        }

        compilation_types::compiled_skiff if_directive::compile(compilation_types::compilation_scope *env)
        {
            vector<string> content;
            content.push_back("if (" + this->condition->compile(env).get_line() + ")");
            content.push_back("{");

            auto * scoped_env = new compilation_types::compilation_scope(env, false);

            for(statement * s : this->body)
            {
                compiled_skiff compiled_line = s->compile(scoped_env);
                if(compiled_line.content.size() == 1)
                {
                    content.push_back("\t" + compiled_line.get_line() + ";");
                }
                else
                {
                    for(string line : compiled_line.content)
                    {
                        content.push_back("\t" + line);
                    }
                }
            }

            content.push_back("}");
            return compiled_skiff(content);
        }

        compilation_types::compiled_skiff comparison::compile(compilation_types::compilation_scope *env)
        {
            compiled_skiff p1 = this->s1->compile(env);
            compiled_skiff p2 = this->s2->compile(env);

            string comp_sign = this->comparison_string();

            if(p1.type.get_name() == "String")
            {
                env->add_include("string.h", false);
                return "strcmp (" + p1.get_line() + ", " + p2.get_line() + ") " + comp_sign + " 0";
            }

            return p1.get_line() + " " + comp_sign + " " + p2.get_line();
        }

        compilation_types::compiled_skiff class_heading::compile(compilation_types::compilation_scope *env)
        {
            auto * innerEnv = new compilation_types::compilation_scope(env, false, this->name, env->get_stack_pointer());

            for(statement * s : body)
            {
                s->compile(innerEnv);
            }

            map<string, size_t> offset_table;
            size_t running_total = 0;
            for (auto const& v : innerEnv->get_raw_variable_table())
            {
                size_t var_size = v.second.typ.get_c_len();
                offset_table[v.first] = running_total;
                running_total += var_size;
            }

            // For pointer types with values, add init to constructor
            // Generate table for variable locations and their offsets


            return compilation_types::compiled_skiff("");
        }
    }
}
