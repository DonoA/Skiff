#if (defined (_WIN32) || defined (_WIN64))
	#include "stdafx.h"
#endif
#include "statement.h"
#include "utils.h"
#include <iostream>
#include <stdlib.h>

namespace skiff
{
	namespace statements
	{
		using ::std::map;
		using ::std::queue;
		using ::std::string;
		using ::std::vector;

		using ::skiff::environment::skiff_object;
		using ::skiff::environment::skiff_class;
		using ::skiff::environment::skiff_function;
		using ::skiff::environment::scope;
        using ::skiff::environment::skiff_value;

        // It might be worth adding a flag for things that can be deallocated immediately

		skiff_object statement::eval(scope * env)
		{
			return skiff_object();
		}

		skiff_object value::eval(scope * env)
		{
            if (!val.empty() && val.find_first_not_of("0123456789") == std::string::npos)
		    {
                void * v = ::skiff::utils::allocate(atoi(val.c_str()));
			    return skiff_object(new skiff_value(v), env->get_type("Int"));
		    }
		    else if (!val.empty() && val.find_first_not_of("0123456789.") == std::string::npos)
		    {
                void * v = ::skiff::utils::allocate(atof(val.c_str()));
			    return skiff_object(new skiff_value(v), env->get_type("Double"));
		    }
		    else if (!val.empty() && val[0] == '"' && val[val.length() - 1] == '"')
            {
                void * s = (void *) new string(val.substr(1, val.length() - 2));
                return skiff_object(new skiff_value(s), env->get_type("String"));
            }
            else if (!val.empty() && val[0] == '\'' && val[val.length() - 1] == '\'')
            {
                return skiff_object(new skiff_value((void *) new string(val)), env->get_type("Sequence"));
            }
            else if (!val.empty() && (val == "true" || val == "false"))
            {
                bool b;
                if(val == "true")
                {
                    b = true;
                }
                else
                {
                    b = false;
                }
                return skiff_object(new skiff_value(::skiff::utils::allocate(b)), env->get_type("Boolean"));
            }
            return skiff_object();
        }

        skiff_object assignment::eval(environment::scope * env)
        {
            skiff_object obj = name->eval(env);
            obj.update_value(val->eval(env).get_value()->get_value());
            std::cout << "Assign comlete:" << std::endl;
            return obj;
        }

        skiff_object math_statement::eval(scope * env)
		{
			queue<char> ops = operators;
			queue<statement *> stmts = operands;
			skiff_object collect = stmts.front()->eval(env);
			stmts.pop();
			while (!ops.empty())
			{
				collect = math_statement::eval_single_op(collect, ops.front(), stmts.front()->eval(env));
				stmts.pop();
				ops.pop();
			}
			return collect;
		}
        
		skiff_object math_statement::eval_single_op(skiff_object s1, char op, skiff_object s2)
		{
			skiff_class * clazz = math_statement::get_dominant_class(s1, s2);
			if (clazz == nullptr)
			{
				clazz = s1.get_class();
			}
			vector<skiff_object> p = {
                s1,
                s2,
                skiff_object(new skiff_value((void *) clazz), nullptr)
            };
			return (*clazz->get_operators())[string(1, op)].eval(p);
		}

        skiff_class * math_statement::get_dominant_class(skiff_object s1, skiff_object s2)
        {
                string type_order[] = {
					"Double"
					"Float",
					"Long",
					"Int",
					"Char"
				};
				for (string s : type_order)
				{
					if (s1.get_class()->get_name() == s)
					{
						return s1.get_class();
					}
					if (s1.get_class()->get_name() == s)
					{
						return s2.get_class();
					}
				}
				return nullptr;
        }

		skiff_object function_call::eval(scope * env)
		{
			if (name == "print")
			{
				string tp;
				for (statement * stmt : params)
				{
					skiff_object res = stmt->eval(env);
					res = res.get_class()->get_scope()->get_function("to_string").eval(res);
					tp += *((string *) res.get_value()->get_value());
					tp += " ";
				}
				tp.erase(tp.length() - 1);
				std::cout << tp << std::endl;
			}
			return skiff_object();
		}

		skiff_object variable::eval(environment::scope * env)
		{
			return env->get_variable(name);
		}

		skiff_object self_modifier::eval(environment::scope * env)
		{
			skiff_object obj = on->eval(env);
			vector<skiff_object> params = {
				obj,
				skiff_object(new skiff_value(::skiff::utils::allocate(1)), env->get_type("Int"))
			};
			(*obj.get_class()->get_operators())[std::string(1, '+')].eval(params);
			std::cout << "Completed inc" << std::endl;
			return obj;
		}

		environment::skiff_object decleration_with_assignment::eval(environment::scope * env)
		{
			skiff_class * clazz = type.eval_class(env);
			vector<skiff_object> params = {
				value->eval(env)
			};
			env->define_variable(name, clazz->construct(params));
			std::cout << "Def and Assign Complete!" << std::endl;
			env->print_debug();
			return environment::skiff_object();
		}
    }

}