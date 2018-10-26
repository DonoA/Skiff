#pragma once

#include "statement.h"

#include <string>
#include <vector>
#include <set>
#include <fstream>
#include <iostream>
#include <map>
#include <optional>

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
            compiled_skiff(string content, statements::type_statement typ) : content({content}), type(typ) { }
            compiled_skiff(vector<string> content) : compiled_skiff(content, statements::type_statement()) { }
            compiled_skiff(vector<string> content, statements::type_statement typ) : content(content), type(typ) { }

            string get_line() { return content.at(0); }
            vector<string> content;
            statements::type_statement type;
        };

        class scratch_manager
        {
        public:
            void allocate_var(string name, size_t length);
            size_t get_var(string name);
        private:
            size_t current;
            map<string, size_t> offsets;
        };

        class compilation_scope
        {
        public:
            struct c_function {
                string compiled_name;
                string proto;
                vector<string> content;
                statements::type_statement return_type;
            };

            struct marked_var_commit {
                vector<string> preamble;
                vector<string> commits;
            };

            compilation_scope() : compilation_scope(nullptr, false) {}
            compilation_scope(compilation_scope * parent, bool functional_scope);

            void add_include(string name, bool local);
            void declare_function(string real_name, string comp_name, string proto, vector<string> content,
                                  statements::type_statement returns);
            void unroll(std::ofstream * output);
            string get_running_id();
            void define_variable(string name, statements::type_statement class_name);
            statements::type_statement get_variable(string name);
            c_function get_function(string name);

            marked_var_commit commit_marked_vars();
        private:
            struct var_search {
                bool found;
                statements::type_statement result;
            };
            var_search get_internal_variable(string name);
            scratch_manager * get_scratch_manager();
            void define_global_function(string proto, vector<string> content);

            compilation_scope * parent;
            set<string> marked_vars;
            size_t running_id = 0;
            map<string, bool> includes;
            scratch_manager scratch;
            map<string, c_function> local_functions;
            vector<c_function> global_functions;
            bool functional_scope = false;
            map<string, statements::type_statement> variable_table;
        };
    }
}


