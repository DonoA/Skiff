#pragma once
#include <string>
#include <vector>

using std::string;
using std::vector;



string remove_pad(string str);

vector<string> string_split(string str, string d);

string generate_indent(size_t len);