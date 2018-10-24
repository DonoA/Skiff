#pragma once

#include "statement.h"

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
    namespace statements
    {
        class type_statement;
    }

    namespace compilation_types
    {
        class compiled_skiff
        {
        public:
            compiled_skiff(string str) : compiled_skiff(str, statements::type_statement()) { }
            compiled_skiff(string content, statements::type_statement typ) : content(content), type(typ) { }
            string content;
            statements::type_statement type;
        };

        class compilation_scope
        {
        public:
            struct include {
                string name;
                bool local;
            };
            struct c_function {
                string proto;
                string content;
            };
            void add_include(string name, bool local);
            void declare_function(string proto, string content);
            void add_to_main_function(string content);
            void unroll(std::ofstream * output);
            string get_running_id();
            void define_variable(string name, statements::type_statement class_name);
            statements::type_statement get_variable(string name);
        private:
            size_t running_id = 0;
            vector<include> includes;
            vector<c_function> defined_functions;
            map<string, statements::type_statement> variable_table;
        };
    }
}


