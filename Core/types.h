#pragma once
#include <string>
#include <map>
#include <vector>
#include <stdlib.h>
#include <functional>
#include <iostream>

#include "utils.h"

namespace skiff
{
	namespace types
	{
		class type_class;
		class function;
		class object;

		class scope
		{
		public:
			scope() { inherit = nullptr; };
			scope(scope * inherit);
			void define_variable(std::string name, object * val);
			object * get_variable(std::string name);
			void define_type(std::string name, type_class cls);
			type_class get_type(std::string name);
			void define_function(std::string name, function func);
			function get_function(std::string name);
		private:
			std::map<std::string, object *> env;
			scope * inherit;
			std::map<std::string, type_class> known_types;
			std::map<std::string, function> known_functions;
		};

		class type_class
		{
		public:
			type_class();
			type_class(std::string name);
			type_class(std::string name, std::vector<type_class> generic_types);
			type_class(std::string name, size_t id);
			std::string get_name();
			size_t get_class_id();
			std::string parse_string();
			scope * get_scope();
			std::map<std::string, function> * get_operators();
		private:
			std::string name;
			std::vector<type_class> generic_types;
			scope class_env;
			size_t class_id;
			std::map<std::string, function> operators;
			static size_t internal_class_id_counter;
		};

		class object
		{
		public:
			template<class T>
			static void * allocate(T val);
			object() : object(nullptr, type_class("")) { }
			object(void * str, type_class type);
			type_class get_type();
			std::string to_string();
			void * get_value();
			void set_value(void * v);
		private:
			type_class type;
			void * value;
		};

		class function
		{
		public:
			struct function_parameter
			{
				type_class typ;
				std::string name;
			};
			static function::function_parameter create_function_parameter(std::string name, 
				type_class typ);
			function(std::string name, std::vector<function_parameter> params, type_class returns, 
				scope * env, 
				std::function<object(object, std::vector<object>, scope *)> * builtin);
			//function(std::string name, std::vector<function_parameter> params, type_class returns, scope * env, object * (*builtin)(object *, std::vector<object *>, scope *));
			function(std::string name, std::vector<function_parameter> params, 
				type_class returns, scope * env);
			function(std::string name, scope * env, 
				std::function<object(object, std::vector<object>, scope *)> * builtin);
			function();
			object eval(object self);
			object eval(object self, std::vector<object> params);
		private:
			scope function_env;
			std::string name;
			std::vector<function_parameter> params;
			type_class returns;
			//std::vector<statement *> statements;
			//object * (*builtin)(object *, std::vector<object *>, scope *);
			std::function<object(object, std::vector<object>, scope *)> * builtin;
		};

		template<class T>
		inline void * object::allocate(T val)
		{
			T * p = (T *)malloc(sizeof(T));
			(*p) = val;
			return p;
		}
	}
}
