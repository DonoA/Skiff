#pragma once
#include <string>
#include <vector>
#include "types.h"
#include "statement.h"

using std::string;
using std::vector;

string remove_pad(string str);

vector<string> string_split(string str, string d);

string generate_indent(size_t len);

object * get_dominant_type(object * c1, object * c2);