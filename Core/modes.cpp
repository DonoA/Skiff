#include "modes.h"

namespace skiff
{
    namespace modes
    {
        using ::std::string;
        using ::std::vector;
        using ::std::queue;
        using ::std::stack;
        using ::std::ofstream;
        using statements::statement;
        using statements::braced_block;
        using environment::scope;

        vector<statement *> parse_file(string infile)
        {
            std::fstream fin(infile, std::fstream::in);
            string file;
            string line;
            while (std::getline(fin, line))
            {
                file += line + "\n";
            }
            vector<tokenizer::token> token_sequence = tokenizer::tokenize(file);
            vector<statement *> statements = parser(token_sequence).parse();

            return statements;
        }

        void evaluate(scope *env, vector<statement *> statements)
        {
            for (statement *s : statements)
            {
                s->eval(env);
            }
        }

        void compile(compilation_types::compilation_scope *env, vector<statement *> statements, string outfile)
        {
            ofstream output;
            output.open(outfile);
            for (statement *s : statements)
            {
                std::cout << s->compile(env) << ";\n";
            }
            output.close();
        }
    }
}
