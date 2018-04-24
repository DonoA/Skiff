#include "stdafx.h"
#include "types.h"

namespace skiff
{
	namespace environment
	{
		using ::std::string;
		using ::std::vector;
		using ::std::map;

		using ::skiff::environment::skiff_object;
		using ::skiff::environment::skiff_class;
		using ::skiff::environment::skiff_function;
		using ::skiff::environment::scope;

		skiff_object::skiff_object(void * val, skiff_class * type)
		{
			this->type = type;
			this->value = val;
		}

		skiff_class * skiff_object::get_class()
		{
			return type;
		}

		string skiff_object::to_string()
		{
			return type->get_name() + "@" + std::to_string((int)this);
		}

		void * skiff_object::get_value()
		{
			return value;
		}

		void skiff_object::set_value(void * v)
		{
			this->value = v;
		}

		skiff_function::function_parameter skiff_function::create_function_parameter(
			std::string name, skiff_class * typ)
		{
			return function_parameter();
		}

		skiff_function::skiff_function(string name, vector<function_parameter> params, 
			skiff_class * returns, scope * env)
		{
			this->function_env = new scope(env);
			this->name = name;
			this->params = params;
			this->returns = returns;
			this->builtin = nullptr;
		}

		skiff_function::skiff_function(std::string name, scope * env, 
			std::function<skiff_object(skiff_object, std::vector<skiff_object>, scope*)>* builtin)
		{
			this->name = name;
			this->function_env = new scope(env);
			this->params = vector<function_parameter>();
			this->returns = nullptr;
			this->builtin = builtin;
		}

		skiff_object skiff_function::eval(skiff_object self)
		{
			return eval(self, vector<skiff_object>());
		}

		skiff_object skiff_function::eval(skiff_object self, vector<skiff_object> params)
		{
			if (builtin == nullptr)
			{
				//for (statement * stmt : statements)
				//{
				//	stmt->eval(&function_env);
				//}
				return skiff_object();
			}
			else
			{
				return (*builtin)(self, params, this->function_env);
			}
		}
		scope::scope(scope * inherit)
		{
			this->inherit = inherit;
		}

		void scope::define_variable(string name, skiff_object val)
		{
			env[name] = val;
		}

		skiff_object scope::get_variable(string name)
		{
			if (env.count(name))
			{
				return env[name];
			}
			if (inherit == nullptr)
			{
				return skiff_object();
			}
			return inherit->get_variable(name);
		}

		void scope::define_type(string name, skiff_class cls)
		{
			known_types[name] = cls;
		}

		skiff_class scope::get_type(string name)
		{
			if (known_types.count(name))
			{
				return known_types[name];
			}
			if (inherit == nullptr)
			{
				return skiff_class();
			}
			return inherit->get_type(name);
		}

		void scope::define_function(string name, skiff_function cls)
		{
			known_functions[name] = cls;
		}

		skiff_function scope::get_function(string name)
		{
			if (known_functions.count(name))
			{
				return known_functions[name];
			}
			if (inherit == nullptr)
			{
				return skiff_function();
			}
			return inherit->get_function(name);
		}
		skiff_class::skiff_class(std::string name, skiff_class * parent)
		{
			this->name = name;
			this->parent = parent;
		}
		scope * skiff_class::get_scope()
		{
			return class_env;
		}
		std::string skiff_class::get_name()
		{
			return name;
		}
		std::map<std::string, skiff_function>* skiff_class::get_operators()
		{
			return &ops;
		}
		void skiff_class::add_constructor(skiff_function constructor_)
		{
			this->constructor = constructor_;
		}
		skiff_object skiff_class::construct(std::vector<skiff_object> params)
		{
			return skiff_object();
		}
	}
}
