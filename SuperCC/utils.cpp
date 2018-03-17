#include "stdafx.h"
#include "utils.h"
#include <iostream>

string remove_pad(string str)
{
	if (str == "")
	{
		return str;
	}
	int i, j;
	for (i = 0; str[i] == ' ' || str[i] == '\n' || str[i] == '\r'; i++);
	for (j = str.length() - 1; j >= 0 && (str[j] == ' ' || str[j] == '\n' || str[j] == '\r'); j--);

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
		rtn += "  ";
	}
	return rtn;
}
