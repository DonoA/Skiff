#include "statement.h"
#include "utils.h"
#include <iostream>

using skiff::compilation_types::compilation_scope;

namespace skiff
{
    namespace statements
    {
        string statement::compile(compilation_scope *env)
        {
            return raw;
        }

        string value::compile(compilation_scope *env)
        {
            return val.get_type() == tokenizer::literal_type::STRING ?
                   "\"" + val.get_value() + "\"" : val.get_value();
        }

        string variable::compile(compilation_scope *env)
        {
            return name;
        }

        string assignment::compile(compilation_scope *env)
        {
            return this->name->compile(env) + " = " + this->val->compile(env);
        }

        string declaration_with_assignment::compile(compilation_scope *env)
        {
            return type.compile(env) + " " + name + " = " + value->compile(env);
        }

        string type_statement::compile(compilation_scope *env)
        {
            if(name == "skiff.lang.Int")
            {
                return "signed int";
            }
            return name;
        }

        string function_call::compile(compilation_scope *env)
        {
            vector<string> compile_params;
            string compiled_name = name.compile(env);
            if(compiled_name == "print")
            {
                compiled_name = "printf";
                for(statement *s : params)
                {
                    compile_params.push_back(s->compile(env));
                }
                return name.compile(env) + "(" + utils::join(compile_params, ",") + ")";
            }

            for(statement *s : params)
            {
                compile_params.push_back(s->compile(env));
            }
            return name.compile(env) + "(" + utils::join(compile_params, ",") + ")";
        }
    }
}
