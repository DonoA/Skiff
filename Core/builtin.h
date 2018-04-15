#pragma once
#include <vector>
#include <functional>
#include "types.h"

using std::vector;

class navite_builtin
{
public:
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_add();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_sub();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_mul();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_div();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_mod();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_clone();
	template<class T>
	static std::function<object *(object *, vector<object *>, scope *)> * create_to_string();

};

class string_builtin
{
public:
	static object * to_string(object * self, vector<object *> params, scope * env);
	static object * clone(object * self, vector<object *> params, scope * env);
};

class builtin
{

public:
	enum type { Int, Float, Double, Char, Long, String, None };
	static type get_type_for(size_t id);
	static size_t get_id_for(type nt);
	static string get_name_for(type nt);
	static string get_c_type_for(builtin::type nt);
};

class builtin_load
{
public:
	template<class T>
	static type_class define_native_builtins(scope * env, builtin::type nt);
	template<class T>
	static type_class define_native_fixpoint_builtins(scope * env, builtin::type nt);
	static type_class define_string_builtins(scope * env);
	static void load_standards(scope * env);
};

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_add()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) + (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_sub()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) - (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_mul()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) * (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_div()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) / (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_mod()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T * n1 = (T *)self->get_value();
		T * n2 = (T *)params[0]->get_value();
		(*n1) = (*n1) % (*n2);
		return nullptr;
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_clone()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T v = *(T *)self->get_value();
		return new object(object::allocate(v), self->get_type());
	});
}

template<class T>
inline std::function<object*(object*, vector<object*>, scope*)>* navite_builtin::create_to_string()
{
	return new std::function<object *(object *, vector<object *>, scope *)>([](object * self, vector<object *> params, scope * env)
	{
		T v = *(T *)self->get_value();
		string * s = new string(std::to_string(v));
		return new object((void *)s, env->get_type("String"));
	});
}

template<class T>
inline type_class builtin_load::define_native_builtins(scope * env, builtin::type nt)
{
	type_class t = type_class(builtin::get_name_for(nt), builtin::get_id_for(nt));
	(*t.get_operators())[string(1, '+')] = function("add", env, navite_builtin::create_add<T>());
	(*t.get_operators())[string(1, '-')] = function("sub", env, navite_builtin::create_sub<T>());
	(*t.get_operators())[string(1, '*')] = function("mul", env, navite_builtin::create_mul<T>());
	(*t.get_operators())[string(1, '/')] = function("div", env, navite_builtin::create_div<T>());
	t.get_scope()->define_function("to_string", function("to_string", env, navite_builtin::create_to_string<T>()));
	t.get_scope()->define_function("clone", function("clone", env, navite_builtin::create_clone<T>()));
	return t;
}

template<class T>
inline type_class builtin_load::define_native_fixpoint_builtins(scope * env, builtin::type nt)
{
	type_class t = define_native_builtins<T>(env, nt);
	(*t.get_operators())[string(1, '%')] = function("mod", vector<function::function_parameter>(), type_class(), env, navite_builtin::create_mod<T>());
	return t;
}
