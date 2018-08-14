#include "evaluated_types.h"

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

        skiff_value::skiff_value(void *val, skiff_class *clazz) {
            this->value = val;
            this->clazz = clazz;
        }

        skiff_class *skiff_value::get_class() {
            return this->clazz;
        }

        void skiff_value::set_value(void *val) {
            this->value = val;
        }

        void *skiff_value::get_value() {
            return this->value;
        }

        void skiff_value::set_class(skiff_class *clazz) {
            this->clazz = clazz;
        }

        skiff_object skiff_value::invoke(std::string name, std::vector<skiff_object> params)
        {

            return skiff_object();
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

        void skiff_object::set_value(skiff_value *v) {
            this->value = v;
        }

        skiff_object::skiff_object() {
            this->value = nullptr;
            this->type = nullptr;
        }

        skiff_object::skiff_object(skiff_class *type)
        {
            this->type = type;
            this->value = nullptr;
        }

        skiff_object::skiff_object(void *val, skiff_class *clazz) {
            this->value = new skiff_value(val, clazz);
            this->type = clazz;
        }

        void *skiff_object::get_raw_value() {
            return this->get_value()->get_value();
        }

        skiff_class *skiff_object::get_value_class() {
            return this->value->get_class();
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

		void scope::set_variable(string name, skiff_object val)
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

		void scope::define_class(string name, skiff_class * cls)
		{
			skiff_object class_wrapper = skiff_object((void *) cls, this->get_class_type());
			env[name] = class_wrapper;
		}

		skiff_class * scope::get_type(string name)
		{
			if (env.count(name))
			{
				skiff_object wrapped_class = env[name];
				return (skiff_class *) wrapped_class.get_raw_value();
			}
			if (inherit == nullptr)
			{
				return nullptr;
			}
			return inherit->get_type(name);
		}

		void scope::define_function(string name, skiff_function * func)
		{
			skiff_object function_wrapper = skiff_object((void *) func, this->get_type("skiff.lang.Function"));
			env[name] = function_wrapper;
		}

		skiff_function * scope::get_function(string name)
		{
			if (env.count(name))
			{
				skiff_object wrapped_function = env[name];
				return (skiff_function *) wrapped_function.get_raw_value();
			}
			if (inherit == nullptr)
			{
				return nullptr;
			}
			return inherit->get_function(name);
		}
		void scope::print_debug()
		{
			std::cout << "== Known Variables ==" << std::endl;
			for(auto it = env.begin(); it != env.end(); ++it) {
				std::cout << it->first << std::endl;
			}
		}
		string scope::get_debug_string()
		{
			string rtn = "";
			for(auto it = env.begin(); it != env.end(); ++it) {
				string str;
				skiff_function * to_string = it->second.get_value()->get_class()->get_scope()->get_function("to_string");
				if(to_string != nullptr) {
				    str = to_string->eval(it->second).get_value_as<string>();
				} else {
				    str = it->second.to_string();
				}
				rtn += it->first + ": " + it->second.get_class()->get_name() + " = " + str + " (" + it->second.get_value()->get_class()->get_name() + ");\n";
			}
			return rtn;
		}

        void scope::define_class_type(skiff_class *cls)
        {
            skiff_object class_wrapper = skiff_object((void *) cls, cls);
            // TODO: remove this hard coded name
            env["skiff.lang.Class"] = class_wrapper;
        }

        skiff_class *scope::get_class_type() {
            return this->get_type("skiff.lang.Class");
        }

        void scope::define_struct(std::string name, skiff_class *cls) {
            skiff_object class_wrapper = skiff_object((void *) cls, this->get_type("skiff.lang.Struct"));
            env[name] = class_wrapper;
        }

        skiff_class::skiff_class(std::string name, bool isval, skiff_class * parent)
		{
			this->name = name;
			this->isval = isval
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
		void skiff_class::add_operator(builtin_operation key, skiff_function op)
		{
			ops[key] = op;
		}
		skiff_object skiff_class::invoke_operator(builtin_operation op, vector<skiff_object> params)
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

        bool skiff_class::is_val() {
            return this->isval;
        }

        skiff_class::skiff_class(std::string name, skiff_class *parent) : skiff_class(name, false, parent) {
            if(parent != nullptr)
            {
                this->isval = parent->is_val();
            }
        }

    }
}
