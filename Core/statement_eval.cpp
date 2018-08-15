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
		using ::skiff::environment::builtin_operation;

		// It might be worth adding a flag for things that can be deallocated immediately

		skiff_object statement::eval(scope * env)
		{
			return skiff_object();
		}

		environment::skiff_object type_statement::eval(environment::scope *env) {
			return skiff_object((void *) env->get_type(this->name), env->get_type("skiff.lang.Class"));
		}

		skiff_object value::eval(scope * env)
		{
            if (!val.empty() && val.find_first_not_of("0123456789") == std::string::npos)
		    {
			    return skiff_object(atoi(val.c_str()), env->get_type("skiff.lang.Int"));
		    }
		    else if (!val.empty() && val.find_first_not_of("0123456789.") == std::string::npos)
		    {
			    return skiff_object(atof(val.c_str()), env->get_type("skiff.lang.Double"));
		    }
		    else if (!val.empty() && val[0] == '"' && val[val.length() - 1] == '"')
            {
                return skiff_object(string(val.substr(1, val.length() - 2)), env->get_type("skiff.lang.String"));
            }
            else if (!val.empty() && val[0] == '\'' && val[val.length() - 1] == '\'')
            {
                return skiff_object(string(val), env->get_type("skiff.lang.Sequence"));
            }
            return env->get_none_object();
        }

		environment::skiff_object boolean_value::eval(environment::scope *env) {
			return skiff_object(val, env->get_type("skiff.lang.Boolean"));
		}

        skiff_object assignment::eval(environment::scope * env)
        {
            skiff_object obj = name->eval(env);
            skiff_object new_obj = val->eval(env);

			obj.assign_reference_value(new_obj);

            return obj;
        }

        // Math in functions does not parse correctly

//        skiff_object math_statement::eval(scope * env)
//		{
//			queue<char> ops = operators;
//			queue<statement *> stmts = operands;
//			skiff_object collect = stmts.front()->eval(env);
//			stmts.pop();
//			while (!ops.empty())
//			{
//				collect = math_statement::eval_single_op(collect, ops.front(), stmts.front()->eval(env));
//				stmts.pop();
//				ops.pop();
//			}
//			return collect;
//		}
        
		skiff_object math_statement::eval_single_op(skiff_object s1, char op, skiff_object s2)
		{
//			skiff_class * clazz = math_statement::get_dominant_class(s1, s2);
//			if (clazz == nullptr)
//			{
//				clazz = s1.get_class();
//			}
//			vector<skiff_object> p = {
//                s1,
//                s2,
//                skiff_object(new skiff_value((void *) clazz), nullptr)
//            };
//			return clazz->invoke_operator(string(1, op), p);
			return skiff_object();
		}

        skiff_class * math_statement::get_dominant_class(skiff_object s1, skiff_object s2)
        {
//                string type_order[] = {
//					"Double"
//					"Float",
//					"Long",
//					"Int",
//					"Char"
//				};
//				for (string s : type_order)
//				{
//					if (s1.get_class()->get_name() == s)
//					{
//						return s1.get_class();
//					}
//					if (s1.get_class()->get_name() == s)
//					{
//						return s2.get_class();
//					}
//				}
			return nullptr;
        }

		skiff_object function_call::eval(scope * env)
		{
//			if (name == "print")
//			{
//				string tp;
//				for (statement * stmt : params)
//				{
//					skiff_object res = stmt->eval(env);
//					res = res.get_class()->get_scope()->get_function("to_string").eval(res);
//					tp += *((string *) res.get_value()->get_value());
//					tp += " ";
//				}
//				tp.erase(tp.length() - 1);
//				std::cout << tp << std::endl;
//			}
			return skiff_object();
		}

		skiff_object variable::eval(environment::scope * env)
		{
			return env->get_variable(name);
		}

		skiff_object self_modifier::eval(environment::scope * env)
		{
			skiff_object obj = on->eval(env);
			vector<skiff_object> p = {
					obj
			};
			skiff_value * return_obj_value = new skiff_value(obj.get_raw_value(), obj.get_class());
			switch(this->type)
			{
				case modifier_type::PLUS:
					obj.get_value()->set_value(
							obj.get_class()->invoke_operator(builtin_operation::INC, p).get_raw_value()
					);
					break;
				case modifier_type::MINUS:
					obj.get_value()->set_value(
							obj.get_class()->invoke_operator(builtin_operation::DEC, p).get_raw_value()
					);
					break;
			}
			if(this->time == modifier_time::POST) {
				delete return_obj_value;
				return_obj_value = new skiff_value(obj.get_raw_value(), obj.get_class());
			}
			skiff_object rtn_obj = skiff_object(obj.get_class());
			rtn_obj.set_value(return_obj_value);
			return rtn_obj;
		}

		environment::skiff_object declaration_with_assignment::eval(environment::scope * env)
		{
			skiff_class * clazz = (skiff_class *) type.eval(env).get_raw_value();
			skiff_object new_obj = skiff_object(clazz);
			new_obj.assign_reference_value(value->eval(env));
			env->set_variable(name, new_obj);
			return env->get_none_object();
		}

        skiff_object if_directive::eval(environment::scope * env)
        {
			skiff_object obj = condition->eval(env);
			if(obj.get_value_as<bool>())
			{
				scope inner_scope = scope(env);
				this->eval_body(&inner_scope);
			}
//			else
//			{
//				if(else_block != nullptr)
//				{
//					else_block->eval(env);
//				}
//			}
            return env->get_none_object();
        }

//		skiff_object else_heading::eval(environment::scope * env)
//		{
//			if(wrapping == nullptr)
//			{
//				body->eval(env);
//			}
//			else
//			{
//				wrapping->eval(env);
//			}
//			return skiff_object();
//		}

		skiff_object comparison::eval(environment::scope * env)
		{
			skiff_object o1 = s1->eval(env);
			skiff_object o2 = s2->eval(env);
			vector<skiff_object> p = {
				o1, o2
			};
			switch (typ) {
				case EQUAL:
					return o1.get_class()->invoke_operator(builtin_operation::EQUAL, p);
				case NOT_EQUAL: {
					skiff_object res = o1.get_class()->invoke_operator(builtin_operation::EQUAL, p);
					bool *b = (bool *) res.get_raw_value();
					*b = !(*b);
					return res;
				}
				case LESS_THAN:
					return o1.get_class()->invoke_operator(builtin_operation::LESS, p);
				case LESS_THAN_EQUAL_TO: {
					skiff_object lt = o1.get_class()->invoke_operator(builtin_operation::LESS, p);
					skiff_object et = o1.get_class()->invoke_operator(builtin_operation::EQUAL, p);
					bool lt_b = lt.get_value_as<bool>();
					bool et_b = et.get_value_as<bool>();

					bool lt_e = lt_b || et_b;

					return skiff_object(lt_e, env->get_type("skiff.lang.Boolean"));
				}
				case GREATER_THAN:
					return o1.get_class()->invoke_operator(builtin_operation::GREATER, p);
				case GREATER_THAN_EQUAL_TO: {
					skiff_object gt = o1.get_class()->invoke_operator(builtin_operation::GREATER, p);
					skiff_object et = o1.get_class()->invoke_operator(builtin_operation::EQUAL, p);
					bool gt_b = gt.get_value_as<bool>();
					bool et_b = et.get_value_as<bool>();

					bool gt_e = gt_b || et_b;

					return skiff_object(gt_e, env->get_type("skiff.lang.Boolean"));
				}
			}
		}

		void block_heading::eval_body(environment::scope *env)
		{
			for(statement * s : body)
			{
				s->eval(env);
			}
		}
    }
}