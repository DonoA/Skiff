#include "compilation_types.h"

namespace skiff
{
    namespace compilation_types
    {
        void compilation_scope::add_include(string name, bool local)
        {
            if(this->parent == nullptr)
            {
                this->includes[name] = local;
            }
            else
            {
                this->parent->add_include(name, local);
            }
        }

        void compilation_scope::declare_function(string real_name, string comp_name, string proto,
                                                 vector<string> content, statements::type_statement returns)
        {
            this->local_functions[real_name]  = {comp_name, proto, content, returns};
            if(this->parent != nullptr)
            {
                this->parent->define_global_function(proto, content);
            }
            else
            {
                this->define_global_function(proto, content);
            }
        }

        void compilation_scope::unroll(std::ofstream * output)
        {
            for(auto const& include : includes)
            {
                (*output) << "#include " << (include.second ? "\"" : "<") << include.first << (include.second ? "\"" : ">") << std::endl;
            }

            for(auto const& func : this->global_functions)
            {
                (*output) << func.proto << ";" << std::endl;
            }

            (*output) << "uint8_t * heap; // Pre allocated memory space" << std::endl <<
                         "size_t heap_offset;" << std::endl <<
                         "uint8_t * stack;" << std::endl;

            for(auto const& func : this->global_functions)
            {
                (*output) << func.proto << "\n{\n";
                for(string s : func.content)
                {
                    (*output) << s << "\n";
                }
                (*output) << "}\n";
            }
        }

        string compilation_scope::get_running_id()
        {
            if(this->parent == nullptr)
            {
                this->running_id++;
                return std::to_string(this->running_id);
            }
            else
            {
                return this->parent->get_running_id();
            }
        }

        compilation_scope::c_var compilation_scope::define_variable(string name, statements::type_statement class_name)
        {
            c_var new_var = {stack_pointer, class_name};
            this->variable_table[name] = new_var;
            stack_pointer += class_name.get_c_len();
            return new_var;
        }

        const compilation_scope::c_var& compilation_scope::get_variable(string name)
        {
            auto it = variable_table.find(name);
            if(it != variable_table.end())
            {
                return it->second;
            }
            if(this->parent != nullptr)
            {
                var_search search = this->parent->get_internal_variable(name);
                if(search.found)
                {
                    this->marked_vars.insert(name);
                    return search.result;
                }
            }
            throw;
        }

        compilation_scope::compilation_scope(compilation_scope *parent, bool functional_scope, string prefix, size_t stack_pointer) :
                includes(),
                local_functions(),
                local_classes(),
                global_functions(),
                variable_table(),
                parent(parent),
                functional_scope(functional_scope),
                stack_pointer(stack_pointer)
        {
            if(parent == nullptr)
            {
                this->prefix = prefix + "_";
            }
            else
            {
                this->prefix = this->parent->get_prefix() + prefix + "_";
            }
            this->add_include("stdlib.h", false);
            this->add_include("string.h", false);
            this->add_include("stdint.h", false);
        }

        compilation_scope::c_function compilation_scope::get_function(string name)
        {
            auto it = local_functions.find(name);
            if(it != local_functions.end())
            {
                return it->second;
            }
            if(this->parent != nullptr)
            {
                return this->parent->get_function(name);
            }
            std::cout << "Func not found for " << name << std::endl;
            return c_function();
        }

        compilation_scope::var_search compilation_scope::get_internal_variable(string name)
        {
            auto it = variable_table.find(name);
            if(it != variable_table.end())
            {
                return {true, it->second};
            }
            if(this->parent != nullptr)
            {
                var_search search = this->parent->get_internal_variable(name);
                if(search.found)
                {
                    this->marked_vars.insert(name);
                    return search;
                }
            }
            throw;
        }

        compilation_scope::marked_var_commit compilation_scope::commit_marked_vars()
        {
            marked_var_commit commit = {vector<string>(),vector<string>()};
            for(string s : this->marked_vars)
            {
//                statements::type_statement ts = this->get_variable(s);
//                string type_name = ts.compile(this).get_line();
//                this->allocate_var(s, ts.get_c_len());
//                string offset = std::to_string(sm->get_var(s));
//                if(!ts.is_ref_type())
//                {
//                    commit.preamble.push_back(
//                            type_name + " " + s + " = *((" + type_name + " *)(stack + " + offset + "))");
//                    // commits are not parsed as single lines and thus the semicolon needs to be added here
//                    commit.commits.push_back("*((" + type_name + " *)(stack + " + offset + ")) = " + s + ";");
//                }
            }
            return commit;
        }

        void compilation_scope::define_global_function(string proto, vector<string> content)
        {
            if(this->parent == nullptr)
            {
                this->global_functions.push_back({"", proto, content, statements::type_statement()});
            }
            else
            {
                this->parent->define_global_function(proto, content);
            }
        }

        void compilation_scope::declare_class(string real_name, string comp_name, map<string, size_t> fields,
                                              size_t total_size)
        {
            this->local_classes[real_name] = {comp_name, fields, total_size};
        }
    }
}
