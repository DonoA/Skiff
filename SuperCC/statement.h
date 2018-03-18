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

class object;
class scope;

class statement
{
public:
	statement();
	statement(string raw);
	virtual string eval_c();
	virtual object * eval(scope * env);
	virtual string parse_string();
private:
	string raw;
};

class value : public statement
{
public:
	value(object * val);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	object * val;
	type_class typ;
};

class variable : public statement
{
public:
	variable(string name);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
};

class math_statement : public statement
{
public:
	math_statement(queue<statement *> operands, queue<char> operators);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	queue<statement*> operands;
	queue<char> operators;
	static void eval_single_op(object * s1, char op, object * s2);
};

class assignment : public statement
{
public:
	assignment(string name, statement * value);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	statement * val;
};

class decleration : public statement
{
public:
	decleration(string name, type_class type, statement * value);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	type_class type;
	statement * val;
};

class function_call : public statement
{
public:
	function_call(string name, vector<statement *> params);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	std::vector<statement *> params;
};

class block_heading : public statement
{
public:
	block_heading() {};
	block_heading(string raw);
	virtual string eval_c();
	virtual object * eval(scope * env);
	virtual string parse_string() = 0;
	void add_statement(statement * stmt);
protected:
	vector<statement *> statements;
private:
	string raw;
};

class if_heading : public block_heading
{
public:
	if_heading(statement * condition);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * condition;
};

class while_heading : public block_heading
{
public:
	while_heading(statement * condition);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * condition;
};

class class_heading : public block_heading
{
public:
	class_heading(string name);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
};

class function_heading : public block_heading
{

public:
	function_heading(string name, vector<function::function_parameter> params, type_class returns);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	string name;
	vector<function::function_parameter> params;
	type_class returns;
	string function_parameter_sig(function::function_parameter);
};

class comparison : public statement
{
public:
	enum comparison_type { Equal, NotEqual, LessThan, LessThanEqualTo, GreaterThan, GreaterThanEqualTo };
	comparison(statement * s1, comparison::comparison_type typ, statement * s2);
	object * eval(scope * env);
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
	invert(statement * value);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * val;
};

class bitinvert : public statement
{
public:
	bitinvert(statement * value);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	statement * val;
};

class bitwise : public statement
{
public:
	enum operation { And, Or, Xor };
	bitwise(statement * s1, bitwise::operation op, statement * s2);
	object * eval(scope * env);
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
	boolean_conjunction(comparison * s1, boolean_conjunction::conjunction_type conj, comparison * s2);
	object * eval(scope * env);
	string eval_c();
	string parse_string();
private:
	comparison * s1;
	comparison * s2;
	boolean_conjunction::conjunction_type conj;
	string conj_string();
};