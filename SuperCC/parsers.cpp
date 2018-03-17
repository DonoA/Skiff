#include "stdafx.h"
#include "parsers.h"
#include "utils.h"
#include <iostream>
#include <queue>

using std::queue;

void check_back_brace(char op, stack<char> * braces)
{
	if (braces->empty() || braces->top() != op)
	{
		std::cout << "brace mismatch" << std::endl;
	}
	else
	{
		braces->pop();
	}
}

void try_push(char op, stack<char> * braces)
{
	if (!braces->empty() && braces->top() == op)
	{
		braces->pop();
	}
	else
	{
		braces->push(op);
	}
}

void track_braces(char lc, char c, stack<char> * braces)
{
	switch (c)
	{
	case '[':
	case '(':
	case '{':
		braces->push(c);
		break;
	case '"':
		if (lc != '\\')
		{
			try_push(c, braces);
		}
	case '\'':
		if (lc != '\\')
		{
			try_push(c, braces);
		}
		break;
	case ']':
		check_back_brace('[', braces);
		break;
	case ')':
		check_back_brace('(', braces);
		break;
	case '}':
		check_back_brace('{', braces);
		break;
	}
}

vector<string> parse_argument_list(string list)
{
	vector<string> params;
	stack<char> braces;
	int j = 0;
	for (unsigned i = 0; i < list.length(); i++)
	{
		track_braces(i == 0 ? '\0' : list[i - 1], list[i], &braces);
		if (list[i] == ',' && braces.empty())
		{
			params.push_back(list.substr(j, i - j));
			j = i + 1;
		}
	}
	if (list != "")
	{
		params.push_back(list.substr(j));
	}
	return params;
}

type * box_type(string stmt)
{
	//std::cout << "boxing " << stmt << std::endl;
	if (!stmt.empty() && stmt.find_first_not_of("0123456789") == std::string::npos)
	{
		return new Int(stmt);
	}
	else if (!stmt.empty() && stmt.find_first_not_of("0123456789.") == std::string::npos)
	{
		return new Double(stmt);
	}
	else if (!stmt.empty() && stmt[0] == '"' && stmt[stmt.length() - 1] == '"')
	{
		return new String(stmt);
	}
	return nullptr;
}

statement * declare_or_assign(string name, statement * value, scope * env, string stmt)
{
	vector<string> s = string_split(name, ":");
	string n = s[0], t;
	if (s.size() == 2)
	{
		t = n[1];
	}
	if (env->get(n) == nullptr)
	{
		return new decleration(n, value, stmt);
	}
	else
	{
		return new assignment(n, value, stmt);
	}
}

bool is_math(char c)
{
	return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
}

statement * parse_math(string stmt, scope * env)
{
	stack<char> braces;
	queue<char> operators;
	queue<statement *> operands;
	int j = 0;
	for (unsigned i = 0; i < stmt.length(); i++)
	{
		track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
		if (is_math(stmt[i]) && braces.empty())
		{
			operands.push(parse_statement(stmt.substr(j, i - j), env));
			operators.push(stmt[i]);
			j = i + 1;
		}
	}
	if (stmt != "")
	{
		operands.push(parse_statement(stmt.substr(j), env));
	}
	return new math_statement(operands, operators, stmt);
}

statement * parse_function_header(string stmt, scope * env) 
{
	size_t j, i = j = stmt.find_first_of("(");
	string dec = string_split(stmt.substr(0, i), " ")[1];
	string params;
	stack<char> braces;
	for (; i < stmt.length(); i++)
	{
		track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
		if (stmt[i] == ')' && braces.empty())
		{
			params = stmt.substr(j + 1, i - j - 1);
			break;
		}
	}
	string returns = stmt.substr(i+1);
	returns.erase(0, returns.find_first_of(":") + 1);
	std::cout << dec << " | " << params << " | " << returns << std::endl;
	vector<string> params_list = parse_argument_list(params);
	vector<function_heading::function_parameter> f_params;
	for (string s : params_list)
	{
		vector<string> n = string_split(s, ":");
		f_params.push_back(function_heading::create_function_parameter(n[0], new TypeClass(n[1])));
	}
	return new function_heading(dec, f_params, new TypeClass(returns), stmt);
}

statement * parse_statement(string stmt, scope * env)
{
	stmt = remove_pad(stmt);
	string p1, p2;
	bool parsing_string = false;
	for (unsigned i = 0; i < stmt.length(); i++)
	{
		if ((stmt[i] == '"' || stmt[i] == '\'') && (i == 0 || stmt[i-1] != '\\'))
		{
			parsing_string = !parsing_string;
		}
		if (!parsing_string)
		{
			if (stmt[i] == '=' && stmt[i + 1] != '=')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 1);
				return declare_or_assign(remove_pad(p1), parse_statement(p2, env), env, stmt);
			}
			else if (stmt[i] == '!')
			{
				p2 = remove_pad(stmt.substr(i + 1));
				return new invert(parse_statement(p2, env), stmt);
			}
			else if (stmt[i] == '~')
			{
				p2 = remove_pad(stmt.substr(i + 1));
				return new bitinvert(parse_statement(p2, env), stmt);
			}
			else if (stmt[i] == '(')
			{
				p1 = remove_pad(stmt.substr(0, i));
				p2 = stmt.substr(i + 1, stmt.find_last_of(')') - (i + 1));
				if (p1 == "")
				{
					return parse_statement(p2, env);
				}
				else if (p1 == "if")
				{
					return new if_heading(parse_statement(p2, env), stmt);
				}
				else if (p1 == "while")
				{
					return new while_heading(parse_statement(p2, env), stmt);
				}
				else
				{
					vector<string> parts = string_split(p1, " ");
					if (parts[0] == "def")
					{
						return parse_function_header(stmt, env);
					}
					vector<string> params = parse_argument_list(p2);
					vector<statement *> param_stmt;
					for (string s : params)
					{
						param_stmt.push_back(parse_statement(s, env));
					}
					return new function_call(p1, param_stmt, stmt);
				}
			}
			else if (is_math(stmt[i]))
			{
				if (i + 1 < stmt.length() && stmt[i + 1] == '=')
				{
					p1 = remove_pad(stmt.substr(0, i));
					p2 = stmt.substr(i + 2);
					queue<char> ops;
					ops.push(stmt[i]);
					queue<statement *> stmts;
					stmts.push(new variable(p1));
					stmts.push(parse_math(p2, env));
					return new assignment(p1, new math_statement(stmts, ops, p2), stmt);
				}
				return parse_math(stmt, env);
			}
			else if (stmt[i] == '<' || stmt[i] == '>' || (stmt[i] == '!' && stmt[i+1] == '=') || stmt[i] == '=')
			{
				p1 = remove_pad(stmt.substr(0, i));
				p2 = remove_pad(stmt.substr(i + 1));
				comparison::comparison_type typ = comparison::comparison_type::Equal;
				if (stmt[i] == '<')
				{
					typ = comparison::comparison_type::LessThan;
					if (stmt[i + 1] == '=')
					{
						typ = comparison::comparison_type::LessThanEqualTo;
						p2 = remove_pad(stmt.substr(i + 2));
					}
				}
				else if (stmt[i] == '>')
				{
					typ = comparison::comparison_type::GreaterThan;
					if (stmt[i + 1] == '=')
					{
						typ = comparison::comparison_type::GreaterThanEqualTo;
						p2 = remove_pad(stmt.substr(i + 2));
					}
				}
				else if (stmt[i] == '!')
				{
					typ = comparison::comparison_type::NotEqual;
					p2 = remove_pad(stmt.substr(i + 2));
				}
				return new comparison(parse_statement(p1, env), typ, parse_statement(p2, env), stmt);
			}
			else if (stmt[i] == '&' || stmt[i] == '|')
			{
				if (stmt[i + 1] == '&' || stmt[i + 1] == '|')
				{
					p1 = stmt.substr(0, i);
					p2 = stmt.substr(i + 2);
					comparison * c1 = (comparison *)parse_statement(p1, env);
					comparison * c2 = (comparison *)parse_statement(p2, env);
					boolean_conjunction::conjunction_type typ = boolean_conjunction::conjunction_type::And;
					if (stmt[i + 1] == '|')
					{
						typ = boolean_conjunction::conjunction_type::Or;
					}
					return new boolean_conjunction(c1, typ, c2, stmt);
				}
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 1);
				statement * s1 = parse_statement(p1, env);
				statement * s2 = parse_statement(p2, env);
				bitwise::operation op = bitwise::operation::And;
				if (stmt[i] == '|')
				{
					op = bitwise::operation::Or;
				}
				return new bitwise(s1, op, s2, stmt);
			}
			else if (stmt[i] == '^')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 1);
				statement * s1 = parse_statement(p1, env);
				statement * s2 = parse_statement(p2, env);
				bitwise::operation op = bitwise::operation::Xor;
				return new bitwise(s1, op, s2, stmt);
			}
		}
	}
	stmt = remove_pad(stmt);
	if (stmt == "")
	{
		return new statement(stmt);
	}
	type * t;
	if ((t = box_type(stmt)) != nullptr)
	{
		return new value(t, stmt);
	}
	if ((t = env->get(stmt)) != nullptr)
	{
		return new variable(stmt);
	}
	vector<string> parts = string_split(stmt, " ");
	if (parts[0] == "class")
	{
		return new class_heading(parts[1], stmt);
	}
	std::cerr << "Could not locate refrence: '" << stmt << "'" << std::endl;
	return new statement(stmt);
}
