#include "stdafx.h"
#include "types.h"

namespace skiff
{
	namespace types
	{
		using ::std::string;
		using ::std::vector;
		using ::std::map;

		size_t type_class::internal_class_id_counter = 100;

		type_class::type_class()
		{
			this->name = "None";
		}

		type_class::type_class(string name)
		{
			this->name = name;
			this->generic_types = vector<type_class>();
			this->class_id = type_class::internal_class_id_counter++;
		}

		type_class::type_class(string name, vector<type_class> generic_types) : type_class(name)
		{
			this->generic_types = generic_types;
		}

		type_class::type_class(string name, size_t id)
		{
			this->name = name;
			this->class_id = id;
		}

		string type_class::get_name()
		{
			return name;
		}

		size_t type_class::get_class_id()
		{
			return class_id;
		}

		string type_class::parse_string()
		{
			if (generic_types.empty())
			{
				return "TypeClass(" + name + ")";
			}
			string params_rtn = "Generics(";
			bool any = false;
			for (type_class tc : generic_types)
			{
				params_rtn += tc.parse_string() + ",";
				any = true;
			}
			if (any)
			{
				params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
			}
			return "TypeClass(" + name + ", " + params_rtn + "))";
		}

		scope * type_class::get_scope()
		{
			return &class_env;
		}

		map<string, function>* type_class::get_operators()
		{
			return &operators;
		}

		object::object(void * val, type_class type)
		{
			this->type = type;
			this->value = val;
		}

		type_class object::get_type()
		{
			return type;
		}

		string object::to_string()
		{
			return type.get_name() + "@" + std::to_string((int)this);
		}

		void * object::get_value()
		{
			return value;
		}

		void object::set_value(void * v)
		{
			this->value = v;
		}

		function::function_parameter function::create_function_parameter(string name, 
			type_class typ)
		{
			function::function_parameter p;
			p.typ = typ;
			p.name = name;
			return p;
		}

		function::function(string name, vector<function_parameter> params, type_class returns, 
			scope * env, std::function<object(object, vector<object>, scope *)> * builtin)
		{
			this->function_env = scope(env);
			this->name = name;
			this->params = params;
			this->returns = returns;
			this->builtin = builtin;

		}

		function::function(string name, vector<function_parameter> params, type_class returns, 
			scope * env) : function(name, params, returns, env, NULL)
		{ }

		function::function(string name, scope * env, std::function<object(object, vector<object>,
			scope*)>* builtin) : function(name, 
				vector<function_parameter>(), type_class(), env, builtin)
		{
		}

		function::function()
		{
			this->builtin = NULL;
		}

		object function::eval(object self)
		{
			return eval(self, vector<object>());
		}

		object function::eval(object self, vector<object> params)
		{
			if (builtin == NULL)
			{
				//for (statement * stmt : statements)
				//{
				//	stmt->eval(&function_env);
				//}
				return object();
			}
			else
			{
				return (*builtin)(self, params, &function_env);
			}
		}
		scope::scope(scope * inherit)
		{
			this->inherit = inherit;
		}

		void scope::define_variable(string name, object * val)
		{
			env[name] = val;
		}

		object * scope::get_variable(string name)
		{
			if (env.count(name))
			{
				return env[name];
			}
			if (inherit == nullptr)
			{
				return nullptr;
			}
			return inherit->get_variable(name);
		}

		void scope::define_type(string name, type_class cls)
		{
			known_types[name] = cls;
		}

		type_class scope::get_type(string name)
		{
			if (known_types.count(name))
			{
				return known_types[name];
			}
			if (inherit == nullptr)
			{
				return type_class();
			}
			return inherit->get_type(name);
		}

		void scope::define_function(string name, function cls)
		{
			known_functions[name] = cls;
		}

		function scope::get_function(string name)
		{
			if (known_functions.count(name))
			{
				return known_functions[name];
			}
			if (inherit == nullptr)
			{
				return function();
			}
			return inherit->get_function(name);
		}
	}
}
