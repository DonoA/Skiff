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

		skiff_value::skiff_value(void * v)
		{
			this->value = v;
		}

		skiff_value::~skiff_value()
		{
			if(this->value != nullptr)
			{
				free(this->value);
			}
		}

		void skiff_value::set_value(void * v)
		{
			this->value = v;
		}

		void * skiff_value::get_value()
		{
			return this->value;
		}

		template<class T>
		skiff_native_value<T>::skiff_native_value(T v) : skiff_value()
		{
			this->v = v;
			value = &this->v;
		}

		template<class T>
		void skiff_native_value<T>::set_value(T v)
		{
			this->v = v;
		}

		template<class T>
		T skiff_native_value<T>::get_value()
		{
			return this->v;
		}

		skiff_object::skiff_object(skiff_value * val, skiff_class * type)
		{
			this->type = type;
			this->value = val;
		}

		skiff_class * skiff_object::get_class()
		{
			return type;
		}

		void skiff_object::set_class(skiff_class * clazz)
		{
			this->type = clazz;
		}

		string skiff_object::to_string()
		{
			return type->get_name() + "@" + std::to_string((size_t)this);
		}

		skiff_value * skiff_object::get_value()
		{
			return value;
		}

		void skiff_object::set_value(skiff_value * v)
		{
			this->value = v;
		}

		void skiff_object::update_value(void * v)
		{
			this->value->set_value(v);
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
			skiff_func_sig * builtin)
		{
			this->name = name;
			this->function_env = new scope(env);
			this->params = vector<function_parameter>();
			this->returns = nullptr;
			this->builtin = builtin;
		}

		skiff_object skiff_function::eval(skiff_object self)
		{
			vector<skiff_object> p = {self};
			return eval(p);
		}

		skiff_object skiff_function::eval(vector<skiff_object> params)
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
				return (*builtin)(params, this->function_env);
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

		skiff_class * scope::get_type(string name)
		{
			if (known_types.count(name))
			{
				return &known_types[name];
			}
			if (inherit == nullptr)
			{
				return nullptr;
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
		void scope::print_debug()
		{
			std::cout << "== Known Variables ==" << std::endl;
			for(map<string, skiff_object>::iterator it = env.begin(); it != env.end(); ++it) {
				std::cout << it->first << std::endl;
			}
		}
		string scope::get_debug_string()
		{
			string rtn = "";
			for(map<string, skiff_object>::iterator it = env.begin(); it != env.end(); ++it) {
				string * str = (string *) it->second.get_class()->get_scope()->get_function("to_string").eval(it->second).get_value()->get_value();
				rtn += it->first + ":" + it->second.get_class()->get_name() + "=" + *str + ";";
			}
			return rtn;
		}
		skiff_class::skiff_class(std::string name, skiff_class * parent)
		{
			this->name = name;
			this->parent = parent;
			this->class_env = new scope();
		}
		scope * skiff_class::get_scope()
		{
			return class_env;
		}
		std::string skiff_class::get_name()
		{
			return name;
		}
		void skiff_class::add_operator(string key, skiff_function op)
		{
			ops[key] = op;
		}
		skiff_object skiff_class::invoke_operator(string op, vector<skiff_object> params)
		{
			return ops[op].eval(params);
		}
		void skiff_class::add_constructor(skiff_function constructor_)
		{
			this->constructor = constructor_;
		}
		skiff_object skiff_class::construct(std::vector<skiff_object> params)
		{
			skiff_object obj = constructor.eval(params);
			obj.set_class(this);
			return obj;
		}

	}
}
