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

            bool requires_allocation = false;
            vector<string> content;
            statements::type_statement type;
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

            struct c_class {
                string compiled_name;
                // maps field names to their offsets
                map<string, size_t> fields;
                size_t total_size;
            };

            struct c_var {
                size_t offset;
                statements::type_statement typ;
            };

            struct marked_var_commit {
                vector<string> preamble;
                vector<string> commits;
            };

            compilation_scope() : compilation_scope(nullptr, false, "", 0) {}
            compilation_scope(compilation_scope * parent, bool functional_scope) : compilation_scope(parent,
                                                                                                     functional_scope, "",
                                                                                                     parent->stack_pointer) { }
            compilation_scope(compilation_scope * parent, bool functional_scope, string name, size_t stack_pointer);

            void add_include(string name, bool local);
            void declare_function(string real_name, string comp_name, string proto, vector<string> content,
                                  statements::type_statement returns);

            void declare_class(string real_name, string comp_name, map<string, size_t> fields, size_t total_size);

            void unroll(std::ofstream * output);
            string get_running_id();
            c_var define_variable(string name, statements::type_statement class_name);
            const c_var& get_variable(string name);
            c_function get_function(string name);

            marked_var_commit commit_marked_vars();

            map<string, c_var>& get_raw_variable_table() { return variable_table; }

            string get_prefix() { return prefix; }
            size_t get_stack_pointer() { return stack_pointer; }
        private:
            struct var_search {
                bool found;
                c_var result;
            };
            var_search get_internal_variable(string name);
            void define_global_function(string proto, vector<string> content);

            compilation_scope * parent;
            set<string> marked_vars;
            size_t running_id = 0;
            map<string, bool> includes;

            map<string, c_function> local_functions;
            map<string, c_class> local_classes;

            vector<c_function> global_functions;
            size_t stack_pointer;

            bool functional_scope = false;
            string prefix;
            map<string, c_var> variable_table;
        };
    }
}


