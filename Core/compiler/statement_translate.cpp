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
                return compiled_skiff("\"" + val.get_value() + "\"", type_statement("String"));
            }

            return compiled_skiff(val.get_value(), type_statement("Int"));

        }

        compiled_skiff variable::compile(compilation_scope *env)
        {
            return {name, env->get_variable(name)};
        }

        compiled_skiff assignment::compile(compilation_scope *env)
        {
            compiled_skiff value = this->val->compile(env);
            return {this->name->compile(env).get_line() + " = " + value.get_line(), value.type};
        }

        compiled_skiff declaration_with_assignment::compile(compilation_scope *env)
        {
            env->define_variable(name, type.get_name());
            return {type.compile(env).get_line() + " " + name + " = " + value->compile(env).get_line()};
        }

        compiled_skiff type_statement::compile(compilation_scope *env)
        {
            if(name == "Int")
            {
                return {"signed int"};
            }
            else if(name == "String")
            {
                return {"unsigned char *"};
            }
            else if(name == "None")
            {
                return {"void"};
            }
            return {name};
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
                return compiled_skiff(compiled_name + "(" + utils::join(compile_params, ",") + ")", env->get_variable(compiled_name));
            }

            for(statement *s : params)
            {
                compile_params.push_back(s->compile(env).get_line());
            }
            return "(*" + name.compile(env).get_line() + ")(" + utils::join(compile_params, ",") + ")";
        }

        compiled_skiff function_definition::compile(compilation_types::compilation_scope *env)
        {
            vector<string> params_named;
            vector<string> params_typed;

            compilation_types::compilation_scope * scoped_env = new compilation_types::compilation_scope(env);

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
            string name = this->name + "_" + env->get_running_id();
            string return_comp = this->returns.compile(env).get_line();
            string sig = return_comp + " " +
                    name + " " +
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
            env->declare_function(sig, body);
            env->define_variable(this->name, this->returns.get_name());

            return {return_comp + " (*" + this->name + ")(" + utils::join(params_typed, ",") + ") = &" + name};
        }

        compiled_skiff math_statement::compile(compilation_types::compilation_scope *env)
        {
            string o = "";
            switch(this->opr)
            {
                case math_statement::ADD:
                    o = "+";
                    break;
            }
            compiled_skiff compiledSkiff = this->statement1->compile(env);
            return {compiledSkiff.get_line() + " " + o + " " + this->statement2->compile(env).get_line(), compiledSkiff.type};
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

            for(statement * s : this->body)
            {
                compiled_skiff compiled_line = s->compile(env);
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
                env->add_include("stdlib.h", false);
                return "strcmp (" + p1.get_line() + ", " + p2.get_line() + ") " + comp_sign + " 0";
            }

            return p1.get_line() + " " + comp_sign + " " + p2.get_line();
        }
    }
}
