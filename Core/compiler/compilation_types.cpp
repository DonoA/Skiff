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

        void compilation_scope::declare_function(string proto, vector<string> content)
        {
            if(this->parent == nullptr)
            {
                this->defined_functions.push_back({proto, content});
            }
            else
            {
                this->parent->declare_function(proto, content);
            }
        }

        void compilation_scope::add_to_main_function(string content)
        {
            if(main_adr == -1)
            {
                main_adr = (int) defined_functions.size();
                c_function func;
                func.proto = "int main (int argc, char **argv)";
                func.content = vector<string>();
                this->defined_functions.push_back(func);
            }
            defined_functions.at(main_adr).content.push_back("\t" + content);
        }

        void compilation_scope::unroll(std::ofstream * output)
        {
            for(auto const& include : includes)
            {
                (*output) << "#include " << (include.second ? "\"" : "<") << include.first << (include.second ? "\"" : ">") << std::endl;
            }

            for(c_function c : this->defined_functions)
            {
                (*output) << c.proto << ";" << std::endl;
            }

            for(c_function c : this->defined_functions)
            {
                (*output) << c.proto << "\n{\n";
                for(string s : c.content)
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
            return this->variable_table[name];
        }

        compilation_scope::compilation_scope(compilation_scope *parent) : includes(), defined_functions(),
                                                                          variable_table(), parent(parent)
        { }
    }
}
