#include "stdafx.h"
#include "parsers.h"
#include "utils.h"
#include <iostream>
#include <queue>
#include <algorithm>
#include <assert.h>

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
		break;
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

vector<statement *> parse_argument_statements(vector<string> params)
{
	vector<statement *> parsed;
	for (string s : params)
	{
		parsed.push_back(parse_statement(s));
	}
	return parsed;
}

type_class box_type(string stmt)
{
	if (!stmt.empty() && stmt.find_first_not_of("0123456789") == std::string::npos)
	{
		return type_class("Int");
	}
	else if (!stmt.empty() && stmt.find_first_not_of("0123456789.") == std::string::npos)
	{
		return type_class("Double");
	}
	else if (!stmt.empty() && stmt[0] == '"' && stmt[stmt.length() - 1] == '"')
	{
		return type_class("String");
	}
	else if (!stmt.empty() && stmt[0] == '\'' && stmt[stmt.length() - 1] == '\'')
	{
		return type_class("Sequence");
	}
	else if (!stmt.empty() && (stmt == "true" || stmt == "false"))
	{
		return type_class("Boolean");
	}
	return type_class("Var");
}

bool is_math(char c)
{
	return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
}

math_statement * parse_math(string stmt)
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
			operands.push(parse_statement(stmt.substr(j, i - j)));
			operators.push(stmt[i]);
			j = i + 1;
		}
	}
	if (stmt != "")
	{
		operands.push(parse_statement(stmt.substr(j)));
	}
	return new math_statement(operands, operators);
}

function_heading * parse_function_header(string stmt)
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
	returns = remove_pad(returns);
	vector<string> params_list = parse_argument_list(params);
	vector<function::function_parameter> f_params;
	for (string s : params_list)
	{
		vector<string> n = string_split(s, ":");
		f_params.push_back(function::create_function_parameter(remove_pad(n[0]), 
			type_class(remove_pad(n[1]))));
	}
	return new function_heading(dec, f_params, type_class(returns));
}

new_object_statement * parse_object_creation(string stmt)
{
	size_t j, i = j = stmt.find_first_of("(");
	string dec = stmt.substr(0, i);
	string params_str;
	stack<char> braces;
	for (; i < stmt.length(); i++)
	{
		track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
		if (stmt[i] == ')' && braces.empty())
		{
			params_str = stmt.substr(j + 1, i - j - 1);
			break;
		}
	}
	vector<statement *> params = parse_argument_statements(parse_argument_list(params_str));
	return new new_object_statement(dec, params);
}

annotation_tag * parse_annotation_tag(string tag)
{
	size_t i = tag.find_first_of('(');
	if (i == string::npos)
	{
		size_t split = std::min(tag.find_first_of(' '), tag.find_first_of('\n'));
		string on = remove_pad(tag.substr(split + 1));
		return new annotation_tag(tag.substr(0, split), vector<statement *>(), 
			parse_statement(on));
	}
	if (i > tag.find_first_of('\n'))
	{
		size_t split = tag.find_first_of('\n');
		string on = remove_pad(tag.substr(split + 1));
		return new annotation_tag(tag.substr(0, split), vector<statement *>(), 
			parse_statement(on));
	}
	string name = tag.substr(0, i);
	size_t j = i = tag.find_first_of("(");
	string params;
	stack<char> braces;
	for (; i < tag.length(); i++)
	{
		track_braces(i == 0 ? '\0' : tag[i - 1], tag[i], &braces);
		if (tag[i] == ')' && braces.empty())
		{
			params = tag.substr(j + 1, i - j - 1);
			break;
		}
	}
	vector<statement *> p = parse_argument_statements(parse_argument_list(params));
	string on = tag.substr(i + 1);
	return new annotation_tag(name, p, parse_statement(on));
}

class_heading * parse_class_heading(class_heading::class_type type, string stmt)
{
	return new class_heading(type, stmt);
}

enum_heading * parse_enum_heading(string stmt)
{
	vector<string> s = string_split(stmt, " ");
	if (s[0] == "struct")
	{
		class_heading * heading = parse_class_heading(class_heading::class_type::STRUCT, s[1]);
		return new enum_heading(heading->get_name(), heading);
	}
	else if (s[0] == "class")
	{
		class_heading * heading = parse_class_heading(class_heading::class_type::CLASS, s[1]);
		return new enum_heading(heading->get_name(), heading);
	}
	return new enum_heading(stmt);
}

statement * parse_decleration(string name, string other)
{
	size_t eq_i = other.find_first_of('=');
	if (eq_i == string::npos)
	{
		return new decleration(name, type_class(other));
	}
	else
	{
		string typ = remove_pad(other.substr(0, eq_i));
		string val = remove_pad(other.substr(eq_i + 1));
		return new decleration_with_assignment(name, type_class(typ), parse_statement(val));
	}
	
}

statement * parse_statement(string stmt)
{
	stmt = remove_pad(stmt);
	string p1, p2;
	bool parsing_string = false;
	stack<char> braces;
	for (size_t i = 0; i < stmt.length(); i++)
	{
		track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
		if (braces.empty() && stmt[i] == '&' && stmt[i + 1] == '&')
		{
			p1 = stmt.substr(0, i);
			p2 = stmt.substr(i + 2);
			comparison * c1 = (comparison *)parse_statement(p1);
			comparison * c2 = (comparison *)parse_statement(p2);
			return new boolean_conjunction(c1, boolean_conjunction::conjunction_type::And, c2);
		}
	}
	for (size_t i = 0; i < stmt.length(); i++)
	{
		track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
		if (braces.empty() && stmt[i] == '|' && stmt[i + 1] == '|')
		{
			p1 = stmt.substr(0, i);
			p2 = stmt.substr(i + 2);
			comparison * c1 = (comparison *)parse_statement(p1);
			comparison * c2 = (comparison *)parse_statement(p2);
			return new boolean_conjunction(c1, boolean_conjunction::conjunction_type::Or, c2);
		}
	}
	for (size_t i = 0; i < stmt.length(); i++)
	{
		track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
		if (braces.empty() && (stmt[i] == '<' || stmt[i] == '>' || (stmt[i] == '!' && 
			stmt[i + 1] == '=') || stmt[i] == '='))
		{
			p1 = remove_pad(stmt.substr(0, i));
			p2 = remove_pad(stmt.substr(i + 1));
			comparison::comparison_type typ;
			if (stmt[i] == '<' && stmt[i - 1] != '<' && stmt[i + 1] != '<')
			{
				typ = comparison::comparison_type::LessThan;
				if (stmt[i + 1] == '=')
				{
					typ = comparison::comparison_type::LessThanEqualTo;
					p2 = remove_pad(stmt.substr(i + 2));
				}
				return new comparison(parse_statement(p1), typ, parse_statement(p2));
			}
			else if (stmt[i] == '>' && stmt[i - 1] != '>' && stmt[i + 1] != '>')
			{
				typ = comparison::comparison_type::GreaterThan;
				if (stmt[i + 1] == '=')
				{
					typ = comparison::comparison_type::GreaterThanEqualTo;
					p2 = remove_pad(stmt.substr(i + 2));
				}
				return new comparison(parse_statement(p1), typ, parse_statement(p2));
			}
			else if (stmt[i] == '!')
			{
				typ = comparison::comparison_type::NotEqual;
				p2 = remove_pad(stmt.substr(i + 2));
				return new comparison(parse_statement(p1), typ, parse_statement(p2));
			}
			else if (stmt[i] == '=' && stmt[i + 1] == '=')
			{
				typ = comparison::comparison_type::Equal;
				p2 = remove_pad(stmt.substr(i + 2));
				return new comparison(parse_statement(p1), typ, parse_statement(p2));
			}
		}
	}
	for (size_t i = 0; i < stmt.length(); i++)
	{
		if ((stmt[i] == '"' || stmt[i] == '\'') && (i == 0 || stmt[i-1] != '\\'))
		{
			parsing_string = !parsing_string;
		}
		if (!parsing_string)
		{
			if (stmt[i] == '!')
			{
				if (stmt[i + 1 == '='])
				{
					continue;
				}
				p2 = remove_pad(stmt.substr(i + 1));
				return new invert(parse_statement(p2));
			}
			else if (stmt[i] == '~')
			{
				p2 = remove_pad(stmt.substr(i + 1));
				return new bitinvert(parse_statement(p2));
			}
			else if (stmt[i] == '@')
			{
				p1 = remove_pad(stmt.substr(1));
				return parse_annotation_tag(p1);
			}
			else if (stmt[i] == ' ')
			{
				p1 = remove_pad(stmt.substr(0, i));
				p2 = remove_pad(stmt.substr(i + 1));
				if (p1 == "new")
				{
					return parse_object_creation(p2);
				}
				else if (p1 == "def")
				{
					return parse_function_header(stmt);
				}
				else if (p1 == "class")
				{
					return parse_class_heading(class_heading::class_type::CLASS, p2);
				}
				else if (p1 == "struct")
				{
					return parse_class_heading(class_heading::class_type::STRUCT, p2);
				}
				else if (p1 == "enum")
				{
					return parse_enum_heading(p2);
				}
				else if (p1 == "static")
				{
					return new modifier(modifier::modifier_type::STATIC, parse_statement(p2));
				}
				else if (p1 == "private")
				{
					return new modifier(modifier::modifier_type::PRIVATE, parse_statement(p2));
				}
				else if (p1 == "case")
				{
					//return parse_case_statement(p2);
				}
				else if (p1 == "throw")
				{
					return new throw_statement(parse_statement(p2));
				}
				else if (p1 == "return")
				{
					return new return_statement(parse_statement(p2));
				}
				else if (p1 == "annotation")
				{
					//return new annotation_heading(p2);
				}

			}
			else if (stmt[i] == '(')
			{
				p1 = remove_pad(stmt.substr(0, i));
				p2 = stmt.substr(i + 1, stmt.find_last_of(')') - (i + 1));
				if (p1 == "")
				{
					return parse_statement(p2);
				}
				else if (p1 == "if")
				{
					return new if_heading(parse_statement(p2));
				}
				else if (p1 == "while")
				{
					return new while_heading(parse_statement(p2));
				}
				else if (p1 == "for")
				{

				}
				else if (p1 == "switch")
				{

				}
				else if (p1 == "match")
				{

				}
				else if (p1 == "import")
				{

				}
				else if (p1 == "")
				{
					// analyse what is in the parens (math or first class function)
				}
				else
				{
					vector<string> params = parse_argument_list(p2);
					vector<statement *> param_stmt;
					for (string s : params)
					{
						param_stmt.push_back(parse_statement(s));
					}
					return new function_call(p1, param_stmt);
				}
			}
			else if (stmt[i] == ':')
			{
				p1 = remove_pad(stmt.substr(0, i));
				p2 = remove_pad(stmt.substr(i + 1));
				return parse_decleration(p1, p2);
			}
			else if (stmt[i] == '=')
			{
				if (stmt[i + 1] == '=' || stmt[i - 1] == '=' || stmt[i - 1] == '>' || stmt[i - 1] == '<' || stmt[i - 1] == '!')
				{
					continue;
				}
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 1);
				return new assignment(remove_pad(p1), parse_statement(p2));
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
					stmts.push(parse_statement(p2));
					return new assignment(p1, new math_statement(stmts, ops));
				}
				if (stmt[i] == '+' && i + 1 < stmt.length() && stmt[i + 1] == '+')
				{
					p1 = remove_pad(stmt.substr(0, i));
					p2 = stmt.substr(i + 2);
					if (p1 == "")
					{
						return new self_modifier(self_modifier::modifier_type::PLUS, 
							self_modifier::modifier_time::PRE, parse_statement(p2));
					}
					if (p2 == "")
					{
						return new self_modifier(self_modifier::modifier_type::PLUS,
							self_modifier::modifier_time::POST, parse_statement(p1));
					}
				}
				if (stmt[i] == '-' && i + 1 < stmt.length() && stmt[i + 1] == '-')
				{
					p1 = remove_pad(stmt.substr(0, i));
					p2 = stmt.substr(i + 2);
					if (p1 == "")
					{
						return new self_modifier(self_modifier::modifier_type::MINUS,
							self_modifier::modifier_time::PRE, parse_statement(p2));
					}
					if (p2 == "")
					{
						return new self_modifier(self_modifier::modifier_type::MINUS,
							self_modifier::modifier_time::POST, parse_statement(p1));
					}
				}
				return parse_math(stmt);
			}
			else if (stmt[i] == '&' || stmt[i] == '|')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 1);
				statement * s1 = parse_statement(p1);
				statement * s2 = parse_statement(p2);
				bitwise::operation op = bitwise::operation::And;
				if (stmt[i] == '|')
				{
					op = bitwise::operation::Or;
				}
				return new bitwise(s1, op, s2);
			}
			else if (stmt[i] == '^')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 1);
				statement * s1 = parse_statement(p1);
				statement * s2 = parse_statement(p2);
				bitwise::operation op = bitwise::operation::Xor;
				return new bitwise(s1, op, s2);
			}
			else if (stmt[i] == '>' && stmt[i + 1] == '>')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 2);
				statement * s1 = parse_statement(p1);
				statement * s2 = parse_statement(p2);
				bitwise::operation op = bitwise::operation::ShiftRight;
				return new bitwise(s1, op, s2);
			}
			else if (stmt[i] == '<' && stmt[i + 1] == '<')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 2);
				statement * s1 = parse_statement(p1);
				statement * s2 = parse_statement(p2);
				bitwise::operation op = bitwise::operation::ShiftLeft;
				return new bitwise(s1, op, s2);
			}
		}
	}
	stmt = remove_pad(stmt);
	if (stmt == "")
	{
		return new statement(stmt);
	}
	if (box_type(stmt).get_name() != "Var")
	{
		return new value(stmt);
	}
	//return new variable(stmt);
	//std::cerr << "Could not locate refrence: '" << stmt << "'" << std::endl;
	return new statement(stmt);
}
