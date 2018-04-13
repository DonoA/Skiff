#pragma once
#include <string>
#include <map>
#include <vector>
#include <stdlib.h>
#include <functional>

using std::string;
using std::vector;
using std::map;

//class scope;
class object;
class type_class;
class function;

class scope
{
public:
	scope() { inherit = nullptr; };
	scope(scope * inherit);
	void define_variable(string name, object * val);
	object * get_variable(string name);
	void define_type(string name, type_class cls);
	type_class get_type(string name);
	void define_function(string name, function func);
	function get_function(string name);
private:
	map<string, object *> env;
	scope * inherit;
	map<string, type_class> known_types;
	map<string, function> known_functions;
};

class type_class
{
public:
	type_class();
	type_class(string name);
	type_class(string name, size_t id);
	string get_name();
	size_t get_class_id();
	string parse_string();
	scope * get_scope();
	map<string, function> * get_operators();
private:
	string name;
	scope class_env;
	size_t class_id;
	map<string, function> operators;
	static size_t internal_class_id_counter;
};

class object
{
public:
	template<class T>
	static void * allocate(T val);
	object(void * str, type_class type);
	type_class get_type();
	string to_string();
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
		string name;
	};
	static function::function_parameter create_function_parameter(string name, type_class typ);
	function(string name, vector<function_parameter> params, type_class returns, scope * env, std::function<object *(object *, vector<object *>, scope *)> * builtin);
	//function(string name, vector<function_parameter> params, type_class returns, scope * env, object * (*builtin)(object *, vector<object *>, scope *));
	function(string name, vector<function_parameter> params, type_class returns, scope * env);
	function(string name, scope * env, std::function<object *(object *, vector<object *>, scope *)> * builtin);
	function();
	object * eval(object * self);
	object * eval(object * self, vector<object *> params);
private:
	scope function_env;
	string name;
	vector<function_parameter> params;
	type_class returns;
	//vector<statement *> statements;
	//object * (*builtin)(object *, vector<object *>, scope *);
	std::function<object *(object *, vector<object *>, scope *)> * builtin;
};

template<class T>
inline void * object::allocate(T val)
{
	T * p = (T *) malloc(sizeof(T));
	(*p) = val;
	return p;
}
