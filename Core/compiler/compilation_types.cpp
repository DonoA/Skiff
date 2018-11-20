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
                         "uint8_t * ref_heap;" << std::endl;

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

        void compilation_scope::define_variable(string name, statements::type_statement class_name)
        {
            this->variable_table[name] = class_name;
        }

        statements::type_statement compilation_scope::get_variable(string name)
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
            return statements::type_statement();
        }

        compilation_scope::compilation_scope(compilation_scope *parent, bool functional_scope, string prefix) :
                includes(),
                local_functions(),
                local_classes(),
                global_functions(),
                variable_table(),
                parent(parent),
                functional_scope(functional_scope)
        {
            if(parent == nullptr)
            {
                this->heap = heap_manager();
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
            return {false, statements::type_statement()};
        }

        compilation_scope::marked_var_commit compilation_scope::commit_marked_vars()
        {
            marked_var_commit commit = {vector<string>(),vector<string>()};
            for(string s : this->marked_vars)
            {
                statements::type_statement ts = this->get_variable(s);
                string type_name = ts.compile(this).get_line();
                heap_manager * sm = this->get_heap_manager();
                sm->allocate_var(s, ts.get_c_len());
                string offset = std::to_string(sm->get_var(s));
                if(!ts.is_ref_type())
                {
                    commit.preamble.push_back(
                            type_name + " " + s + " = *((" + type_name + " *)(ref_heap + " + offset + "))");
                    // commits are not parsed as single lines and thus the semicolon needs to be added here
                    commit.commits.push_back("*((" + type_name + " *)(ref_heap + " + offset + ")) = " + s + ";");
                }
            }
            return commit;
        }

        heap_manager *compilation_scope::get_heap_manager()
        {
            if(this->parent != nullptr)
            {
                return this->parent->get_heap_manager();
            }
            return &this->heap;
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

        void heap_manager::allocate_var(string name, size_t length)
        {
            this->offsets[name] = this->current;
            this->current += length;
        }

        size_t heap_manager::get_var(string name)
        {
            return this->offsets[name];
        }
    }
}
