#pragma once
#include <vector>
#include <functional>
#include <string>
#include <iostream>
#include <map>
#include "types.h"

namespace skiff
{
	namespace builtin
	{
		enum type { Int, Float, Double, Char, Long, String, None };
		type get_type_for(size_t id);
		size_t get_id_for(type nt);
		std::string get_name_for(type nt);
		std::string get_c_type_for(type nt);

		namespace generator
		{
			template<class T>
			std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)> * create_add();
			template<class T>
			std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)> * create_sub();
			template<class T>
			std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)> * create_mul();
			template<class T>
			std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)> * create_div();
			template<class T>
			std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)> * create_mod();
			template<class T>
			std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)> * create_clone();
			template<class T>
			std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)> * create_to_string();

			namespace string
			{
				types::object * to_string(types::object * self, std::vector<types::object *> params, types::scope * env);
				types::object * clone(types::object * self, std::vector<types::object *> params, types::scope * env);
			}
			template<class T>
			inline std::function<types::object*(types::object*, std::vector<types::object*>, types::scope*)>* create_add()
			{
				return new std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)>([](types::object * self, std::vector<types::object *> params, types::scope * env)
				{
					T * n1 = (T *)self->get_value();
					T * n2 = (T *)params[0]->get_value();
					(*n1) = (*n1) + (*n2);
					return nullptr;
				});
			}

			template<class T>
			inline std::function<types::object*(types::object*, std::vector<types::object*>, types::scope*)>* create_sub()
			{
				return new std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)>([](types::object * self, std::vector<types::object *> params, types::scope * env)
				{
					T * n1 = (T *)self->get_value();
					T * n2 = (T *)params[0]->get_value();
					(*n1) = (*n1) - (*n2);
					return nullptr;
				});
			}

			template<class T>
			inline std::function<types::object*(types::object*, std::vector<types::object*>, types::scope*)>* create_mul()
			{
				return new std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)>([](types::object * self, std::vector<types::object *> params, types::scope * env)
				{
					T * n1 = (T *)self->get_value();
					T * n2 = (T *)params[0]->get_value();
					(*n1) = (*n1) * (*n2);
					return nullptr;
				});
			}

			template<class T>
			inline std::function<types::object*(types::object*, std::vector<types::object*>, types::scope*)>* create_div()
			{
				return new std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)>([](types::object * self, std::vector<types::object *> params, types::scope * env)
				{
					T * n1 = (T *)self->get_value();
					T * n2 = (T *)params[0]->get_value();
					(*n1) = (*n1) / (*n2);
					return nullptr;
				});
			}

			template<class T>
			inline std::function<types::object*(types::object*, std::vector<types::object*>, types::scope*)>* create_mod()
			{
				return new std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)>([](types::object * self, std::vector<types::object *> params, types::scope * env)
				{
					T * n1 = (T *)self->get_value();
					T * n2 = (T *)params[0]->get_value();
					(*n1) = (*n1) % (*n2);
					return nullptr;
				});
			}

			template<class T>
			inline std::function<types::object*(types::object*, std::vector<types::object*>, types::scope*)>* create_clone()
			{
				return new std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)>([](types::object * self, std::vector<types::object *> params, types::scope * env)
				{
					T v = *(T *)self->get_value();
					return new types::object(types::object::allocate(v), self->get_type());
				});
			}

			template<class T>
			inline std::function<types::object*(types::object*, std::vector<types::object*>, types::scope*)>* create_to_string()
			{
				return new std::function<types::object *(types::object *, std::vector<types::object *>, types::scope *)>([](types::object * self, std::vector<types::object *> params, types::scope * env)
				{
					T v = *(T *)self->get_value();
					std::string * s = new std::string(std::to_string(v));
					return new types::object((void *)s, env->get_type("String"));
				});
			}
		}

		namespace load
		{
			template<class T>
			types::type_class define_native_builtins(types::scope * env, builtin::type nt);
			template<class T>
			types::type_class define_native_fixpoint_builtins(types::scope * env, builtin::type nt);
			types::type_class define_string_builtins(types::scope * env);
			void load_standards(types::scope * env);

			template<class T>
			inline types::type_class define_native_builtins(types::scope * env, builtin::type nt)
			{
				types::type_class t = types::type_class(builtin::get_name_for(nt), builtin::get_id_for(nt));
				(*t.get_operators())[std::string(1, '+')] = types::function("add", env, generator::create_add<T>());
				(*t.get_operators())[std::string(1, '-')] = types::function("sub", env, generator::create_sub<T>());
				(*t.get_operators())[std::string(1, '*')] = types::function("mul", env, generator::create_mul<T>());
				(*t.get_operators())[std::string(1, '/')] = types::function("div", env, generator::create_div<T>());
				t.get_scope()->define_function("to_string", types::function("to_string", env, generator::create_to_string<T>()));
				t.get_scope()->define_function("clone", types::function("clone", env, generator::create_clone<T>()));
				return t;
			}

			template<class T>
			inline types::type_class define_native_fixpoint_builtins(types::scope * env, builtin::type nt)
			{
				types::type_class t = define_native_builtins<T>(env, nt);
				(*t.get_operators())[std::string(1, '%')] = types::function("mod", std::vector<types::function::function_parameter>(), types::type_class(), env, generator::create_mod<T>());
				return t;
			}
		}

		namespace utils
		{
			types::object * get_dominant_type(types::object * c1, types::object * c2);
		}
	}
}








