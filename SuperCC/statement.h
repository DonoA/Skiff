#pragma once
#include <string>
#include <vector>
#include <map>
#include <queue>
#include "types.h"

using std::string;
using std::vector;
using std::map;
using std::queue;

class scope
{
public:
	scope() {} ;
	scope(scope * inherit);
	void define(string name, type * typ);
	type * get(string name);
private:
	map<string, type *> env;
	scope * inherit;
};

class statement
{
public:
	statement(string raw);
	virtual string eval_c();
	virtual type * eval(scope * env);
	virtual string parse_string();
	virtual bool is_block_heading();
private:
	string raw;
};

class value : public statement
{
public:
	value(type * typ, string val);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	type * typ;
};

class variable : public statement
{
public:
	variable(string name);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
};

class math_statement : public statement
{
public:
	math_statement(queue<statement *> operands, queue<char> operators, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	queue<statement*> operands;
	queue<char> operators;
};

class assignment : public statement
{
public:
	assignment(string name, statement * value, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	statement * val;
};

class decleration : public statement
{
public:
	decleration(string name, statement * value, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	string type;
	statement * val;
};

class function_call : public statement
{
public:
	function_call(string name, vector<statement *> params, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	std::vector<statement *> params;
};

class block_heading : public statement
{
public:
	block_heading(string raw);
	virtual string eval_c();
	virtual type * eval(scope * env);
	virtual string parse_string() = 0;
	virtual bool is_block_heading();
	void add_statement(statement * stmt);
protected:
	vector<statement *> statements;
private:
	string raw;
};

class if_heading : public block_heading
{
public:
	if_heading(statement * condition, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * condition;
};

class while_heading : public block_heading
{
public:
	while_heading(statement * condition, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * condition;
};

class class_heading : public block_heading
{
public:
	class_heading(string name, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
};

class function_heading : public block_heading
{

public:
	struct function_parameter
	{
		TypeClass * typ;
		string name;
	};
	static function_heading::function_parameter create_function_parameter(string name, TypeClass * typ);
	function_heading(string name, vector<function_parameter> params, TypeClass * returns, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	vector<function_parameter> params;
	TypeClass * returns;
	string function_parameter_sig(function_parameter);
};

class comparison : public statement
{
public:
	enum comparison_type { Equal, NotEqual, LessThan, LessThanEqualTo, GreaterThan, GreaterThanEqualTo };
	comparison(statement * s1, comparison::comparison_type typ, statement * s2, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * s1;
	statement * s2;
	comparison_type typ;
	string comparison_string();
};

class invert : public statement
{
public:
	invert(statement * value, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * val;
};

class bitinvert : public statement
{
public:
	bitinvert(statement * value, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * val;
};

class bitwise : public statement
{
public:
	enum operation { And, Or, Xor };
	bitwise(statement * s1, bitwise::operation op, statement * s2, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * s1;
	statement * s2;
	bitwise::operation op;
	string operation_string();
};

class boolean_conjunction : public statement
{
public:
	enum conjunction_type { And, Or };
	boolean_conjunction(comparison * s1, boolean_conjunction::conjunction_type conj, comparison * s2, string raw);
	type * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	comparison * s1;
	comparison * s2;
	boolean_conjunction::conjunction_type conj;
	string conj_string();
};