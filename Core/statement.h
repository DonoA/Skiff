#pragma once
#include <string>
#include <vector>
#include <map>
#include <queue>
#include "types.h"

namespace skiff
{
	namespace statements
	{
		class statement
		{
		public:
			statement();
			statement(std::string raw);
			virtual std::string eval_c();
			virtual types::object * eval(types::scope * env);
			virtual std::string parse_string();
			virtual int indent_mod();
		private:
			std::string raw;
		};

		class value : public statement
		{
		public:
			value(std::string val);
			std::string parse_string();
		private:
			std::string val;
			types::type_class typ;
		};

		class variable : public statement
		{
		public:
			variable(std::string name);
			std::string parse_string();
		private:
			std::string name;
		};

		class modifier_base : public statement
		{
		public:
			modifier_base(statement * on);
			int indent_mod();
		protected:
			statement * on;
		};

		class list_accessor : public statement
		{
		public:
			list_accessor(statement * list, statement * index);
			std::string parse_string();
		protected:
			statement * list;
			statement * index;
		};

		class annotation_tag : public modifier_base
		{
		public:
			annotation_tag(std::string name, std::vector<statement *> params, statement * on);
			std::string parse_string();
		private:
			std::string name;
			std::vector<statement *> params;
		};

		class math_statement : public statement
		{
		public:
			math_statement(std::queue<statement *> operands, std::queue<char> operators);
			std::string parse_string();
		private:
			std::queue<statement*> operands;
			std::queue<char> operators;
			static void eval_single_op(types::object * s1, char op, types::object * s2);
		};

		class compund_statement : public statement
		{
		public:
			compund_statement(std::vector<statement *> operations);
			std::string parse_string();
		private:
			std::vector<statement *> operations;
		};

		class assignment : public statement
		{
		public:
			assignment(statement * name, statement * value);
			std::string parse_string();
		private:
			statement * name;
			statement * val;
		};

		class decleration : public statement
		{
		public:
			decleration(std::string name, types::type_class type);
			std::string parse_string();
		private:
			std::string name;
			types::type_class type;
		};

		class decleration_with_assignment : public statement
		{
		public:
			decleration_with_assignment(statement * name, types::type_class type, statement * val);
			std::string parse_string();
		private:
			statement * name;
			types::type_class type;
			statement * value;
		};

		class function_call : public statement
		{
		public:
			function_call(std::string name, std::vector<statement *> params);
			std::string parse_string();
		private:
			std::string name;
			std::vector<statement *> params;
		};

		class block_heading : public statement
		{
		public:
			block_heading() {};
			block_heading(std::string raw);
			virtual std::string eval_c();
			virtual types::object * eval(types::scope * env);
			virtual std::string parse_string() = 0;
			int indent_mod();
		private:
			std::string raw;
		};

		class end_block_statement : public statement
		{
		public:
			std::string parse_string();
			int indent_mod();
		};

		class flow_statement : public statement
		{
		public:
			enum type { BREAK, NEXT };
			flow_statement(type typ);
			std::string parse_string();
		private:
			flow_statement::type typ;
		};

		class if_heading : public block_heading
		{
		public:
			if_heading(statement * condition);
			std::string parse_string();
		private:
			statement * condition;
		};

		class else_heading : public block_heading
		{
		public:
			else_heading() : else_heading(nullptr) {};
			else_heading(block_heading * wrapping);
			std::string parse_string();
		private:
			block_heading * wrapping;
		};

		class try_heading : public block_heading
		{
		public:
			try_heading() { };
			std::string parse_string();
		};

		class finally_heading : public block_heading
		{
		public:
			finally_heading() { };
			std::string parse_string();
		};

		class catch_heading : public block_heading
		{
		public:
			catch_heading(statement * var);
			std::string parse_string();
		private:
			statement * var;
		};

		class for_classic_heading : public block_heading
		{
		public:
			for_classic_heading(statement * init, statement * condition, statement * tick);
			std::string parse_string();
		private:
			statement * init;
			statement * condition;
			statement * tick;
		};

		class for_itterator_heading : public block_heading
		{
		public:
			for_itterator_heading(statement * val, statement * list);
			std::string parse_string();
		private:
			statement * val;
			statement * list;
		};

		class while_heading : public block_heading
		{
		public:
			while_heading(statement * condition);
			std::string parse_string();
		private:
			statement * condition;
		};

		class switch_heading : public block_heading
		{
		public:
			enum type { SWITCH, MATCH };
			switch_heading(switch_heading::type typ, statement * on);
			std::string parse_string();
		private:
			switch_heading::type typ;
			statement * on;
		};

		class switch_case_heading : public block_heading
		{
		public:
			switch_case_heading(statement * val);
			std::string parse_string();
		private:
			statement * val;
		};

		class match_case_heading : public block_heading
		{
		public:
			match_case_heading(std::string name, types::type_class t) :
				match_case_heading(name, t, std::vector<std::string>()) { };
			match_case_heading(std::string name, types::type_class t, std::vector<std::string> struct_vals);
			std::string parse_string();
		private:
			std::string name;
			types::type_class t;
			std::vector<std::string> struct_vals;
		};

		class class_heading : public block_heading
		{
		public:
			enum class_type { CLASS, STRUCT, ANNOTATION };
			struct heading_generic
			{
				std::string t_name;
				types::type_class extends;
			};
			class_heading(class_heading::class_type type, std::string name);
			class_heading(class_heading::class_type type, std::string name,
				std::vector<heading_generic> generic_types);
			class_heading(class_heading::class_type type, std::string name, types::type_class extends);
			class_heading(class_heading::class_type type, std::string name,
				std::vector<heading_generic> generic_types, types::type_class extends);
			std::string parse_string();
			std::string get_name();
			static class_heading::heading_generic generate_generic_heading(std::string t_name,
				types::type_class extends);
		private:
			std::string name;
			class_heading::class_type type;
			types::type_class extends;
			std::vector<heading_generic> generic_types;
		};

		class enum_heading : public block_heading
		{
		public:
			enum_heading(std::string name);
			enum_heading(std::string name, class_heading * basetype);
			std::string parse_string();
		private:
			std::string name;
			statement * basetype;
		};

		class modifier : public modifier_base
		{
		public:
			enum modifier_type { STATIC, PRIVATE };
			modifier(modifier::modifier_type type, statement * modof);
			std::string parse_string();
		private:
			modifier::modifier_type type;
		};

		class self_modifier : public modifier_base
		{
		public:
			enum modifier_type { PLUS, MINUS };
			enum modifier_time { PRE, POST };
			self_modifier(self_modifier::modifier_type type,
				self_modifier::modifier_time time, statement * on);
			std::string parse_string();
		private:
			self_modifier::modifier_type type;
			self_modifier::modifier_time time;
		};

		class return_statement : public statement
		{
		public:
			return_statement(statement * returns);
			std::string parse_string();
		private:
			statement * returns;
		};

		class import_statement : public statement
		{
		public:
			import_statement(std::string import_name);
			std::string parse_string();
		private:
			std::string import_name;
		};

		class throw_statement : public statement
		{
		public:
			throw_statement(statement * throws);
			std::string parse_string();
		private:
			statement * throws;
		};

		class new_object_statement : public statement
		{
		public:
			new_object_statement(types::type_class type, std::vector<statement *> params);
			std::string parse_string();
		private:
			types::type_class type;
			std::vector<statement *> params;
		};

		class function_heading : public block_heading
		{
		public:
			function_heading(std::string name) : function_heading(name, std::vector<types::function::function_parameter>(),
				types::type_class("")) { }
			function_heading(std::string name, std::vector<types::function::function_parameter> params,
				types::type_class returns);
			std::string parse_string();
		private:
			std::string name;
			std::vector<types::function::function_parameter> params;
			types::type_class returns;
			std::string function_parameter_sig(types::function::function_parameter);
			std::string function_parameter_c_sig(types::function::function_parameter);
		};

		class comparison : public statement
		{
		public:
			enum comparison_type {
				Equal, NotEqual, LessThan, LessThanEqualTo, GreaterThan,
				GreaterThanEqualTo
			};
			comparison(statement * s1, comparison::comparison_type typ, statement * s2);
			std::string parse_string();
		private:
			statement * s1;
			statement * s2;
			comparison_type typ;
			std::string comparison_string();
		};

		class invert : public statement
		{
		public:
			invert(statement * value);
			std::string parse_string();
		private:
			statement * val;
		};

		class bitinvert : public statement
		{
		public:
			bitinvert(statement * value);
			std::string parse_string();
		private:
			statement * val;
		};

		class bitwise : public statement
		{
		public:
			enum operation { And, Or, Xor, ShiftLeft, ShiftRight };
			bitwise(statement * s1, bitwise::operation op, statement * s2);
			std::string parse_string();
		private:
			statement * s1;
			statement * s2;
			bitwise::operation op;
			std::string operation_string();
		};

		class boolean_conjunction : public statement
		{
		public:
			enum conjunction_type { And, Or };
			boolean_conjunction(statement * s1, boolean_conjunction::conjunction_type conj,
				statement * s2);
			std::string parse_string();
		private:
			statement * s1;
			statement * s2;
			boolean_conjunction::conjunction_type conj;
			std::string conj_string();
		};
	}
}