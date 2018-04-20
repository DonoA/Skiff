#pragma once
#include <string>
#include <vector>
#include <stack>
#include <iostream>
#include "types.h"

namespace skiff
{
	namespace utils
	{
		std::string remove_pad(std::string str);

		std::vector<std::string> string_split(std::string str, std::string d);

		std::string generate_indent(size_t len);

		void track_braces(char lc, char c, std::stack<char> * braces);

		std::vector<std::string> braced_split(std::string list, char del);

		template<class T>
		void * allocate(T val);

		template<class T>
		void * allocate(T val)
		{
			T * t = (T *) malloc(sizeof(T));
			(*t) = val;
			return t;
		}
	}
}
