#pragma once

#include <string>
#include <vector>
#include <map>
#include <queue>
#include <stack>
#include "interpreter/evaluated_types.h"
#include "parser/token.h"



namespace skiff
{
    namespace environment
    {
        class scope;

        class skiff_value;

        class skiff_object;

        class skiff_function;

        class skiff_class;
    }

    namespace compilation_types
    {
        struct compiled_skiff;
        class compilation_scope;
    }

    namespace statements
    {
        class braced_block;

        class statement
        {
        public:
            statement() = default;

            explicit statement(std::string raw);

            virtual compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env);

            virtual environment::skiff_object eval(environment::scope *env);

            virtual std::string parse_string();

            virtual int indent_mod();

        private:
            std::string raw;
        };

        class type_statement : public statement
        {
        public:
            type_statement() : type_statement("") { };

            type_statement(string name) : type_statement(name, std::vector<type_statement>(), nullptr) { };

            type_statement(std::string name, std::vector<type_statement> generic_types, type_statement *extends) :
                    name(name), generic_types(generic_types), extends(extends), has_custom_len(false) { };

            std::string parse_string() override;

            virtual environment::skiff_object eval(environment::scope *env) override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;
            string get_c_type();

            bool is_ref_type() const;

            std::string get_name();

            std::string get_c_symbol();

            type_statement with_custom_c_len(size_t len);
            size_t get_c_len() const;

        private:
            std::string name;
            std::vector<type_statement> generic_types;
            type_statement *extends;
            bool has_custom_len;
            size_t custom_len;
        };

        class value : public statement
        {
        public:
            explicit value(tokenizer::literal val) : val(val) { };

            virtual environment::skiff_object eval(environment::scope *env) override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

            std::string parse_string() override;

        private:
            tokenizer::literal val;
//            type_statement typ;
        };

        class compound : public statement
        {
        public:
            explicit compound(vector<statement *> stmts) : stmts(stmts) { };

//            environment::skiff_object eval(environment::scope *env) override;

//            std::string compile(compilation_types::compilation_scope *env) override;

            std::string parse_string() override;

        private:
            vector<statement *> stmts;
        };

        class boolean_value : public statement
        {
        public:
            explicit boolean_value(bool val) : val(val) { };

            environment::skiff_object eval(environment::scope *env) override;

            std::string parse_string() override;

        private:
            bool val;
        };

        class variable : public statement
        {
        public:
            explicit variable(std::string name) : name(name) { };

            std::string parse_string() override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

            environment::skiff_object eval(environment::scope *env) override;

        private:
            std::string name;
        };

        class modifier_base : public statement
        {
        public:
            explicit modifier_base(statement *on);

            int indent_mod() override;

        protected:
            statement *on;
        };

        class list_accessor : public statement
        {
        public:
            list_accessor(statement *list, statement *index);

            std::string parse_string() override;

        protected:
            statement *list;
            statement *index;
        };

        class annotation_tag : public modifier_base
        {
        public:
            annotation_tag(std::string name, std::vector<statement *> params);

            std::string parse_string() override;

        private:
            std::string name;
            std::vector<statement *> params;
        };

        class math_statement : public statement
        {
        public:
            enum op
            {
                ADD, SUB, DIV, MUL, EXP
            };

            math_statement(statement *statement1, math_statement::op op, statement *statement2) :
                    statement1(statement1), opr(op), statement2(statement2) { };

            std::string parse_string() override;

            environment::skiff_object eval(environment::scope * env) override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            statement *statement1;
            math_statement::op opr;
            statement *statement2;

            std::string string_math_op();

            static environment::skiff_class *
            get_dominant_class(environment::skiff_object s1, environment::skiff_object s2);
        };

        class compund_statement : public statement
        {
        public:
            explicit compund_statement(std::vector<statement *> operations);

            std::string parse_string() override;

        private:
            std::vector<statement *> operations;
        };

        class assignment : public statement
        {
        public:
            assignment(statement *name, statement *value);

            std::string parse_string() override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

            environment::skiff_object eval(environment::scope *env) override;

        private:
            statement *name;
            statement *val;
        };

        class declaration : public statement
        {
        public:
            declaration(std::string name, type_statement type);

            std::string parse_string() override;

        private:
            std::string name;
            type_statement type;
        };

        class declaration_with_assignment : public statement
        {
        public:
            declaration_with_assignment(std::string name, type_statement type, statement *val);

            std::string parse_string() override;

            environment::skiff_object eval(environment::scope *env) override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            std::string name;
            type_statement type;
            statement *value;
        };

        class function_call : public statement
        {
        public:
            function_call(type_statement name, std::vector<statement *> params) :
                    name(name), params(params) { };

            std::string parse_string() override;

            environment::skiff_object eval(environment::scope *env) override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            type_statement name;
            std::vector<statement *> params;
        };

        class block_heading : public statement
        {
        public:
            block_heading() = default;

            explicit block_heading(std::vector<statement *> body) : body(body) { };

            environment::skiff_object eval(environment::scope *env) override;

            std::string parse_string() override = 0;

        protected:
            std::vector<statement *> body;

            std::string parse_body();

            void eval_body(environment::scope *env);
        };

        class flow_statement : public statement
        {
        public:
            enum type
            {
                BREAK, NEXT
            };

            explicit flow_statement(type typ);

            std::string parse_string() override;

        private:
            flow_statement::type typ;
        };

        class else_directive : public block_heading
        {
        public:
            explicit else_directive(std::vector<statement *> body) : block_heading(body) { };

            std::string parse_string() override;
//            environment::skiff_object eval(environment::scope * env) override;
        };

        class if_directive : public block_heading
        {
        public:
            explicit if_directive(statement *condition, std::vector<statement *> body) :
                    block_heading(body), condition(condition) { };

            std::string parse_string() override;

            environment::skiff_object eval(environment::scope *env) override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            statement *condition;
        };

        class try_directive : public block_heading
        {
        public:
            explicit try_directive(std::vector<statement *> body) : block_heading(body) { };

            std::string parse_string() override;
        };

        class finally_directive : public block_heading
        {
        public:
            explicit finally_directive(std::vector<statement *> body) : block_heading(body) { };

            std::string parse_string() override;
        };

        class catch_directive : public block_heading
        {
        public:
            explicit catch_directive(statement *var, std::vector<statement *> body) :
                    block_heading(body), var(var) { };

            std::string parse_string() override;

        private:
            statement *var;
        };

        class for_classic_directive : public block_heading
        {
        public:
            for_classic_directive(statement *init, statement *condition, statement *tick,
                                  std::vector<statement *> body) : block_heading(body),
                                                                   init(init), condition(condition), tick(tick) { };

            std::string parse_string() override;

        private:
            statement *init;
            statement *condition;
            statement *tick;
        };

        class for_itterator_directive : public block_heading
        {
        public:
            for_itterator_directive(std::string val, type_statement type, statement *list,
                                    std::vector<statement *> body) :
                    block_heading(body), val(val), type(type), list(list) { };

            std::string parse_string() override;

        private:
            std::string val;
            type_statement type;
            statement *list;
        };

        class while_directive : public block_heading
        {
        public:
            explicit while_directive(statement *condition, std::vector<statement *> body) :
                    block_heading(body), condition(condition) { };

            std::string parse_string() override;

        private:
            statement *condition;
        };

        class switch_directive : public block_heading
        {
        public:
            enum type
            {
                SWITCH, MATCH
            };

            switch_directive(switch_directive::type typ, statement *on,
                             std::vector<statement *> body) : block_heading(body), typ(typ), on(on) { };

            std::string parse_string() override;

        private:
            switch_directive::type typ;
            statement *on;
        };

        class switch_case_directive : public block_heading
        {
        public:
            explicit switch_case_directive(statement *val, std::vector<statement *> body) :
                    block_heading(body), val(val) { };

            std::string parse_string() override;

        private:
            statement *val;
        };

        class match_case_directive : public block_heading
        {
        public:
//            match_case_directive(std::string name, type_statement t) :
//                    match_case_directive(name, t, std::vector<std::string>()) { };
//            match_case_directive(std::string name, type_statement t,
//                std::vector<std::string> struct_vals) : block_heading();
            std::string parse_string() override;

        private:
            std::string name;
            type_statement t;
            std::vector<std::string> struct_vals;
        };

        class class_heading : public block_heading
        {
        public:
            enum class_type
            {
                CLASS, STRUCT, ANNOTATION
            };
            struct heading_generic
            {
                std::string t_name;
                type_statement extends;
            };

            class_heading(class_heading::class_type type, std::string name, vector<statement *> body);

            class_heading(class_heading::class_type type, std::string name,
                          std::vector<heading_generic> generic_types, vector<statement *> body);

            class_heading(class_heading::class_type type, std::string name,
                          type_statement extends, vector<statement *> body);

            class_heading(class_heading::class_type type, std::string name,
                          std::vector<heading_generic> generic_types, type_statement extends, vector<statement *> body);

            std::string parse_string() override;

            std::string get_name();

            static class_heading::heading_generic generate_generic_heading(std::string t_name,
                                                                           type_statement extends);

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            std::string name;
            class_heading::class_type type;
            type_statement extends;
            std::vector<heading_generic> generic_types;
        };

        class enum_heading : public block_heading
        {
        public:
            explicit enum_heading(std::string name);

            enum_heading(std::string name, class_heading *basetype);

            std::string parse_string() override;

        private:
            std::string name;
            statement *basetype;
        };

        class modifier : public modifier_base
        {
        public:
            enum modifier_type
            {
                STATIC, PRIVATE
            };

            modifier(modifier::modifier_type type, statement *modof);

            std::string parse_string() override;

        private:
            modifier::modifier_type type;
        };

        class self_modifier : public modifier_base
        {
        public:
            enum modifier_type
            {
                PLUS, MINUS
            };
            enum modifier_time
            {
                PRE, POST
            };

            self_modifier(self_modifier::modifier_type type,
                          self_modifier::modifier_time time, statement *on);

            std::string parse_string() override;

            environment::skiff_object eval(environment::scope *env) override;

        private:
            self_modifier::modifier_type type;
            self_modifier::modifier_time time;
        };

        class return_statement : public statement
        {
        public:
            explicit return_statement(statement *returns);

            std::string parse_string() override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            statement *returns;
        };

        class import_statement : public statement
        {
        public:
            explicit import_statement(std::string import_name);

            std::string parse_string() override;

        private:
            std::string import_name;
        };

        class throw_statement : public statement
        {
        public:
            explicit throw_statement(statement *throws);

            std::string parse_string() override;

        private:
            statement *throws;
        };

        class new_object_statement : public statement
        {
        public:
            new_object_statement(type_statement type, std::vector<statement *> params);

            std::string parse_string() override;

        private:
            type_statement type;
            std::vector<statement *> params;
        };

        class function_definition : public block_heading
        {
        public:
            struct function_parameter
            {
                function_parameter(std::string name, type_statement typ)
                {
                    this->name = name;
                    this->typ = typ;
                }

                type_statement typ;
                std::string name;
            };

            function_definition(std::string name, std::vector<function_parameter> params,
                                type_statement returns, std::vector<statement *> body) : block_heading(body),
                                                                                         name(name), params(params),
                                                                                         returns(returns) { };

            std::string parse_string() override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            std::string name;
            std::vector<function_parameter> params;
            type_statement returns;

            std::string function_parameter_sig(function_parameter);
        };

        class extern_function_definition : public statement
        {
        public:
            struct function_parameter
            {
                function_parameter(std::string name, type_statement typ)
                {
                    this->name = name;
                    this->typ = typ;
                }

                type_statement typ;
                std::string name;
            };

            extern_function_definition(std::string name, std::vector<function_parameter> params,
                                       type_statement returns) : name(name), params(params), returns(returns) { };

            std::string parse_string() override;

        private:
            std::string name;
            std::vector<function_parameter> params;
            type_statement returns;

            std::string function_parameter_sig(function_parameter);
        };


        class comparison : public statement
        {
        public:
            enum comparison_type
            {
                EQUAL, NOT_EQUAL, LESS_THAN, LESS_THAN_EQUAL_TO, GREATER_THAN,
                GREATER_THAN_EQUAL_TO
            };

            comparison(statement *s1, comparison::comparison_type typ, statement *s2);

            std::string parse_string() override;

            environment::skiff_object eval(environment::scope *env) override;

            compilation_types::compiled_skiff compile(compilation_types::compilation_scope *env) override;

        private:
            statement *s1;
            statement *s2;
            comparison_type typ;

            std::string comparison_string();
        };

        class invert : public statement
        {
        public:
            explicit invert(statement *value);

            std::string parse_string() override;

        private:
            statement *val;
        };

        class bitinvert : public statement
        {
        public:
            explicit bitinvert(statement *value);

            std::string parse_string() override;

        private:
            statement *val;
        };

        class bitwise : public statement
        {
        public:
            enum operation
            {
                AND, OR, XOR, SHIFT_LEFT, SHIFT_RIGHT
            };

            bitwise(statement *s1, bitwise::operation op, statement *s2);

            std::string parse_string() override;

        private:
            statement *s1;
            statement *s2;
            bitwise::operation op;

            std::string operation_string();
        };

        class boolean_conjunction : public statement
        {
        public:
            enum conjunction_type
            {
                AND, OR
            };

            boolean_conjunction(statement *s1, boolean_conjunction::conjunction_type conj,
                                statement *s2);

            std::string parse_string() override;

        private:
            statement *s1;
            statement *s2;
            boolean_conjunction::conjunction_type conj;

            std::string conj_string();
        };
    }
}

#include "compiler/compilation_types.h"
