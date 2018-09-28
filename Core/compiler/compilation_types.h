#pragma once

#include <string>
#include <vector>
#include <set>
#include <fstream>
#include <iostream>
#include <map>

using std::string;
using std::vector;
using std::set;
using std::ofstream;
using std::map;

namespace skiff
{
    namespace compilation_types
    {
        class compilation_scope
        {
        public:
            struct include {
                string name;
                bool local;
            };
            struct c_function {
                string content;
                string proto;
            };
            void add_include(string name, bool local);
            void declare_function(string proto);
            void add_to_top_function(string content);
            void add_to_main_function(string content);
            void unroll();
            void define_variable(string name, string class_name);
        private:
            vector<include> includes;
            vector<c_function> defined_functions;
            map<string, string> variable_table;
        };
    }
}


