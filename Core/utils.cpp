#include "stdafx.h"
#include "utils.h"
#include "builtin.h"
#include <iostream>

string remove_pad(string str)
{
	if (str == "")
	{
		return str;
	}
	int i, j;
	for (i = 0; str[i] == ' ' || str[i] == '\n' || str[i] == '\r' || str[i] == '\t'; i++);
	for (j = str.length() - 1; j >= 0 && (str[j] == ' ' || str[j] == '\n' || str[j] == '\r' || str[i] == '\t'); j--);

	return str.substr(i, (j + 1) - i);
}

vector<string> string_split(string str, string d)
{
	vector<string> rtn;
	size_t pos = 0;
	std::string token;
	while ((pos = str.find(d)) != std::string::npos) {
		token = str.substr(0, pos);
		if (token.length() != 0)
		{
			rtn.push_back(token);
		}
		str.erase(0, pos + d.length());
	}
	rtn.push_back(str);
	return rtn;
}

string generate_indent(size_t len)
{
	string rtn;
	for (size_t i = 0; i < len; i++)
	{
		rtn += "    ";
	}
	return rtn;
}

object * get_dominant_type(object * c1, object * c2)
{
	builtin::type type_order[] = { 
		builtin::type::Double,
		builtin::type::Float,
		builtin::type::Long,
		builtin::type::Int,
		builtin::type::Char
	};
	for (builtin::type s : type_order)
	{
		if (c1->get_type().get_class_id() == builtin::get_id_for(s))
		{
			return c1;
		}
		if (c2->get_type().get_class_id() == builtin::get_id_for(s))
		{
			return c2;
		}
	}
	return nullptr;
}

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

vector<string> braced_split(string list, char del)
{
	vector<string> params;
	stack<char> braces;
	int j = 0;
	for (unsigned i = 0; i < list.length(); i++)
	{
		track_braces(i == 0 ? '\0' : list[i - 1], list[i], &braces);
		if (list[i] == del && braces.empty())
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


