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
            return compiled_skiff(val.get_type() == tokenizer::literal_type::STRING ?
                   "\"" + val.get_value() + "\"" : val.get_value());
        }

        compiled_skiff variable::compile(compilation_scope *env)
        {
            return {name, env->get_variable(name)};
        }

        compiled_skiff assignment::compile(compilation_scope *env)
        {
            compiled_skiff value = this->val->compile(env);
            return {this->name->compile(env).content + " = " + value.content, value.type};
        }

        compiled_skiff declaration_with_assignment::compile(compilation_scope *env)
        {
            env->define_variable(name, type.get_name());
            return {type.compile(env).content + " " + name + " = " + value->compile(env).content};
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
            string compiled_name = name.compile(env).content;
            if(compiled_name == "print")
            {
                compiled_name = "printf";
                env->add_include("stdio.h", false);
                vector<string>  format;
                for(statement *s : params)
                {
                    compiled_skiff p = s->compile(env);
                    compile_params.push_back(p.content);
                    format.push_back(p.type.get_c_symbol());
                }
                compile_params.insert(compile_params.begin(), "\"" + utils::join(format, " ") + "\\n\"");
                return compiled_skiff(compiled_name + "(" + utils::join(compile_params, ",") + ")", env->get_variable(compiled_name));
            }

            for(statement *s : params)
            {
                compile_params.push_back(s->compile(env).content);
            }
            return "(*" + name.compile(env).content + ")(" + utils::join(compile_params, ",") + ")";
        }

        compiled_skiff function_definition::compile(compilation_types::compilation_scope *env)
        {
            vector<string> params_named;
            vector<string> params_typed;
            for(function_parameter fp : this->params)
            {
                params_named.push_back(fp.typ.compile(env).content + " " + fp.name);
                params_typed.push_back(fp.typ.compile(env).content);
            }
            if(this->params.empty())
            {
                params_named.push_back("void");
                params_typed.push_back("void");
            }
            string name = this->name + "_" + env->get_running_id();
            string return_comp = this->returns.compile(env).content;
            string sig = return_comp + " " +
                    name + " " +
                    "(" + utils::join(params_named, ",") + ")";

            string body;
            for(statement * s : this->body)
            {
                body += s->compile(env).content + ";\n";
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
            return {compiledSkiff.content + " " + o + " " + this->statement2->compile(env).content, compiledSkiff.type};
        }

        compiled_skiff return_statement::compile(compilation_types::compilation_scope *env)
        {
            return {"return " + this->returns->compile(env).content};
        }
    }
}
