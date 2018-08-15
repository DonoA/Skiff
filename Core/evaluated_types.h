#pragma once
#include <string>
#include <map>
#include <vector>
#include <stdlib.h>
#include <functional>
#include <iostream>

#include "statement.h"
#include "utils.h"

namespace skiff
{
	namespace environment
	{
		class scope;
		class skiff_value;
		class skiff_object;
		class skiff_function;
		class skiff_class;

		using skiff_func_sig = std::function<skiff_object(std::vector<skiff_object>,scope *)>;

		enum builtin_operation { ADD, SUB, MUL, DIV, EXP, MOD, EQUAL, LESS, GREATER, INC, DEC };

		class skiff_value
        {
        public:
            skiff_value(void * val, skiff_class * clazz);
            template<class T>
            skiff_value(T val, skiff_class * clazz);

            void set_value(void * val);
            void * get_value();

            void set_class(skiff_class * clazz);
            skiff_class * get_class();

            template<class T>
            T get_value_as();

            skiff_object invoke(std::string name, std::vector<skiff_object> params);
//            skiff_object invoke(std::string name, std::vector<skiff_object> params);
        private:
            void * value;
            skiff_class * clazz;
        };

        template<class T>
        skiff_value::skiff_value(T val, skiff_class *clazz) {
            T * t = (T *) malloc(sizeof(T));
            (*t) = val;
            this->value = (void *) t;
            this->clazz = clazz;
        }

        template<class T>
        T skiff_value::get_value_as() {
            T * t = (T *) this->get_value();
            return *t;
        }

        class skiff_object
		{
		public:
			skiff_object();
            skiff_object(skiff_class * type);
            skiff_object(void * val, skiff_class * clazz);

            template<class T>
            skiff_object(T val, skiff_class * clazz);

			skiff_class * get_class();
			void set_class(skiff_class * clazz);
			std::string to_string();
			skiff_value * get_value();
            void * get_raw_value();
            void set_value(skiff_value *);
            skiff_class * get_value_class();

            void assign_reference_value(skiff_object other);

            template<class T>
            T get_value_as();
		private:
			skiff_class * type;
			skiff_value * value;
		};

        template<class T>
        T skiff_object::get_value_as() {
            T * v = (T *) this->value->get_value();
            return *v;
        }

        template<class T>
        skiff_object::skiff_object(T val, skiff_class *clazz) {
            this->value = new skiff_value(val, clazz);
            this->type = clazz;
        }

		class skiff_function
		{
		public:
			struct function_parameter
			{
				skiff_class * typ;
				std::string name;
			};
			static function_parameter create_function_parameter(std::string name,
				skiff_class * typ);
			skiff_function() : skiff_function("", std::vector<function_parameter>(), nullptr, nullptr)
			{ };
			skiff_function(std::string name, std::vector<function_parameter> params,
				skiff_class * returns, scope * env);
			skiff_function(std::string name, scope * env, skiff_func_sig * builtin);
			skiff_object eval(skiff_object self);
			skiff_object eval(std::vector<skiff_object> params);
		private:
			scope * function_env;
			std::string name;
			std::vector<function_parameter> params;
			skiff_class * returns;
			skiff_func_sig * builtin;
		};

		class skiff_class
		{
		public:
            explicit skiff_class(std::string name) : skiff_class(name, false, nullptr) { }
            skiff_class(std::string name, skiff_class * parent);
            skiff_class(std::string name, bool isval, skiff_class * parent);
			scope * get_scope();
			std::string get_name();

			void add_operator(builtin_operation key, skiff_function op);
			skiff_object invoke_operator(builtin_operation op, std::vector<skiff_object> params);
            skiff_object invoke_function(std::string name, std::vector<skiff_object> params);

            bool is_val();
			
			void add_constructor(skiff_function constructor_);
			skiff_object construct(std::vector<skiff_object> params);
		private:
			std::string name;
			skiff_class * parent;
			scope * class_env;
			bool isval;
			skiff_function constructor;
			std::map<builtin_operation, skiff_function> ops;
		};

		class scope
		{
		public:
			scope() : scope(nullptr) { };
			scope(scope * inherit);
			void set_variable(std::string name, skiff_object val);
			skiff_object get_variable(std::string name);

			void define_type(skiff_class * cls, skiff_class * typ);
			skiff_class * get_type(std::string name);

			void define_function(std::string name, skiff_function * func);
			skiff_function * get_function(std::string name);

			skiff_class * get_none_type();

			skiff_object get_none_object();

			void print_debug();
			std::string get_debug_string();
		private:
			std::map<std::string, skiff_object> env;
			scope * inherit;
		};
	}
}
