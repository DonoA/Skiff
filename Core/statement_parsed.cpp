#include "statement.h"
#include "utils.h"
#include <iostream>

namespace skiff
{
	namespace statements
	{
		using ::std::map;
		using ::std::queue;
		using ::std::string;
		using ::std::vector;

		using ::skiff::environment::skiff_object;
		using ::skiff::environment::skiff_class;
		using ::skiff::environment::skiff_function;
		using ::skiff::environment::scope;

		statement::statement() { }

		statement::statement(string raw)
		{
			this->raw = raw;
		}

		string statement::eval_c()
		{
			return raw;
		}

		string statement::parse_string()
		{
			return "Statement(" + raw + ")";
		}

		int statement::indent_mod()
		{
			return 0;
		}

		void statement::add_body(braced_block * bb)
		{
			std::cout << "No body allowed" << std::endl;
		}

		void statement::finalize(std::stack<braced_block *> * stmts)
		{ }

		value::value(string val)
		{
			this->val = val;
		}

		string value::parse_string()
		{
			return "Value(" + val + ")";
		}

		declaration::declaration(string name, type_statement type)
		{
			this->name = name;
			this->type = type;
		}

		string braced_block::parse_string()
		{
			return "BraceBlock()";
		}

		void braced_block::push_body(statement * s)
		{
			stmts.push(s);
		}

		statement * braced_block::get_last()
		{
			return stmts.back();
		}

		string declaration::parse_string()
		{
			return "Decleration(" + name + "," + type.parse_string() + ")";
		}

		function_call::function_call(string name, vector<statement*> params)
		{
			this->name = utils::remove_pad(name);
			this->params = params;
		}

		string function_call::parse_string()
		{
			string rtn = "FunctionCall(" + name + ", Params(";
			bool any = false;
			for (statement * stmt : params)
			{
				rtn += stmt->parse_string() + ",";
				any = true;
			}
			if (any)
			{
				rtn = rtn.substr(0, rtn.length() - 1);
			}
			rtn += "))";
			return rtn;
		}

		variable::variable(string name) : statement(name)
		{
			this->name = name;
		}

		string variable::parse_string()
		{
			return "Variable(" + name + ")";
		}

		assignment::assignment(statement * name, statement * value)
		{
			this->name = name;
			this->val = value;
		}

		string assignment::parse_string()
		{
			return "Assignment(" + name->parse_string() + "," + val->parse_string() + ")";
		}

		math_statement::math_statement(queue<statement*> operands, queue<char> operators)
		{
			this->operands = operands;
			this->operators = operators;
		}



		string math_statement::parse_string()
		{
			string rtn = "MathStatement(";
			queue<char> ops = operators;
			queue<statement *> stmts = operands;
			while (!ops.empty())
			{
				rtn += stmts.front()->parse_string() + " " + ops.front() + " ";
				stmts.pop();
				ops.pop();
			}
			rtn += stmts.front()->parse_string() + ")";
			return rtn;
		}


		comparison::comparison(statement * s1, comparison::comparison_type typ, statement * s2)
		{
			this->s1 = s1;
			this->s2 = s2;
			this->typ = typ;
		}

		string comparison::parse_string()
		{
			return "Comparison(" + s1->parse_string() + " " + this->comparison_string() + " " +
				s2->parse_string() + ")";
		}

		string comparison::comparison_string()
		{
			switch (typ)
			{
			case Equal:
				return "==";
			case NotEqual:
				return "!=";
			case LessThan:
				return "<";
			case LessThanEqualTo:
				return "<=";
			case GreaterThan:
				return ">";
			case GreaterThanEqualTo:
				return ">=";
			}
			return "";
		}

		invert::invert(statement * value)
		{
			this->val = value;
		}

		string invert::parse_string()
		{
			return "Invert(" + val->parse_string() + ")";
		}

		bitinvert::bitinvert(statement * value)
		{
			this->val = value;
		}

		string bitinvert::parse_string()
		{
			return "BitInvert(" + val->parse_string() + ")";
		}

		bitwise::bitwise(statement * s1, bitwise::operation op, statement * s2)
		{
			this->s1 = s1;
			this->op = op;
			this->s2 = s2;
		}

		string bitwise::parse_string()
		{
			return "Bitwise(" + s1->parse_string() + " " + this->operation_string() + " " +
				s2->parse_string() + ")";
		}

		string bitwise::operation_string()
		{
			switch (op)
			{
			case And:
				return "&";
			case Or:
				return "|";
			case Xor:
				return "^";
			case ShiftLeft:
				return "<<";
			case ShiftRight:
				return ">>";
			}
			return "";
		}

		boolean_conjunction::boolean_conjunction(statement * s1,
			boolean_conjunction::conjunction_type conj, statement * s2)
		{
			this->s1 = s1;
			this->s2 = s2;
			this->conj = conj;
		}

		string boolean_conjunction::parse_string()
		{
			return "BooleanConjunction(" + s1->parse_string() + " " + this->conj_string() + " " +
				s2->parse_string() + ")";
		}

		string boolean_conjunction::conj_string()
		{
			switch (conj)
			{
			case And:
				return "&&";
			case Or:
				return "||";
			}
			return "";
		}

		block_heading::block_heading(string raw) : statement(raw)
		{
			this->raw = raw;
		}

		string block_heading::eval_c()
		{
			return string();
		}

		skiff_object block_heading::eval(scope * env)
		{
			return skiff_object();
		}

		int block_heading::indent_mod()
		{
			return 1;
		}

		void block_heading::add_body(braced_block * s)
		{
			body = s;
		}

		if_heading::if_heading(statement * condition)
		{
			this->condition = condition;
			this->else_block = nullptr;
		}

		string if_heading::parse_string()
		{
			return "If(" + condition->parse_string() + ")";
		}

		void if_heading::add_else_block(else_heading * stmt)
		{
			else_block = stmt;
		}

		void else_heading::add_body(braced_block * s)
		{
			if(wrapping == nullptr)
			{
				body = s;
			}
			else
			{
				wrapping->add_body(s);
			}
		}

		void else_heading::add_else_block(else_heading * stmt)
		{
			if(wrapping != nullptr)
			{
				wrapping->add_else_block(stmt);
			}
		}

		class_heading::class_heading(class_heading::class_type type, string name) :
			class_heading(type, name, vector<class_heading::heading_generic>())
		{ }

		class_heading::class_heading(class_heading::class_type type, string name,
			vector<class_heading::heading_generic> generic_types) :
			class_heading(type, name, generic_types, type_statement(""))
		{ }

		class_heading::class_heading(class_heading::class_type type, string name, type_statement extends) :
			class_heading(type, name, vector<class_heading::heading_generic>(), extends)
		{ }

		class_heading::class_heading(class_heading::class_type type, string name,
			vector<heading_generic> generic_types, type_statement extends)
		{
			this->type = type;
			this->name = name;
			this->generic_types = generic_types;
			this->extends = extends;
		}

		string class_heading::parse_string()
		{
			string heading;
			switch (type)
			{
			case CLASS:
				heading = "ClassHeading";
				break;
			case STRUCT:
				heading = "StructHeading";
				break;
			case ANNOTATION:
				heading = "AnnotationHeading";
				break;
			}
			if (generic_types.empty())
			{
				return heading + "(" + name + "," + extends.parse_string() + ")";
			}
			string params_rtn = "Generics(";
			bool any = false;
			for (class_heading::heading_generic p : generic_types)
			{
				params_rtn += "Generic(" + p.t_name + " extends " + p.extends.parse_string() + "),";
				any = true;
			}
			if (any)
			{
				params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
			}
			params_rtn += ")";
			return heading + "(" + name + +"," + params_rtn + "," + extends.parse_string() + ")";
		}

		string class_heading::get_name()
		{
			return name;
		}

		class_heading::heading_generic class_heading::generate_generic_heading(string t_name, 
			type_statement extends)
		{
			return { t_name, extends };
		}


		function_heading::function_parameter function_heading::create_function_parameter(std::string name, type_statement typ)
		{
			function_heading::function_parameter p;
			p.typ = typ;
			p.name = name;
			return p;
		}

		function_heading::function_heading(string name, vector<function_parameter> params,
			type_statement returns)
		{
			this->name = name;
			this->params = params;
			this->returns = returns;
		}

		string function_heading::parse_string()
		{
			string params_rtn = "Params(";
			bool any = false;
			for (function_parameter p : params)
			{
				params_rtn += function_parameter_sig(p) + ",";
				any = true;
			}
			if (any)
			{
				params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
			}
			params_rtn += ")";
			return "FunctionHeading(" + name + ", " + params_rtn +
				", Returns(" + returns.parse_string() + "))";
		}

		string function_heading::function_parameter_sig(function_parameter p)
		{
			return "Param(" + p.name + +"," + p.typ.parse_string() + ")";
		}

		string function_heading::function_parameter_c_sig(function_parameter p)
		{
			//builtin::type t = builtin::get_type_for(p.typ.get_class_id());
			//return builtin::get_c_type_for(t) + " " + p.name;
			return string();
		}

		while_heading::while_heading(statement * condition)
		{
			this->condition = condition;
		}

		string while_heading::parse_string()
		{
			return "While(" + condition->parse_string() + ")";
		}

		string end_block_statement::parse_string()
		{
			return "EndBlock()";
		}

		int end_block_statement::indent_mod()
		{
			return -1;
		}

		return_statement::return_statement(statement * returns)
		{
			this->returns = returns;
		}

		string return_statement::parse_string()
		{
			return "Returns(" + returns->parse_string() + ")";
		}

		new_object_statement::new_object_statement(type_statement type,
			vector<statement*> params)
		{
			this->type = type;
			this->params = params;
		}

		string new_object_statement::parse_string()
		{
			string paramz;
			bool any = false;
			for (statement * p : params)
			{
				paramz += p->parse_string() + ",";
				any = true;
			}
			if (any)
			{
				paramz = paramz.substr(0, paramz.length() - 1);
			}
			return "New(" + type.parse_string() + ", Params(" + paramz + ")";
		}

		annotation_tag::annotation_tag(string tag_name, vector<statement *> params,
			statement * on) : modifier_base(on)
		{
			name = tag_name;
			this->params = params;
		}

		string annotation_tag::parse_string()
		{
			string parms;
			bool any = false;
			for (statement * stmt : params)
			{
				parms += stmt->parse_string() + ",";
				any = true;
			}
			if (any)
			{
				parms = parms.substr(0, parms.length() - 1);
			}
			return "Annotation(" + name + ", Params(" + parms + "), On(" + on->parse_string() + "))";
		}

		enum_heading::enum_heading(string name)
		{
			this->basetype = nullptr;
			this->name = name;
		}

		enum_heading::enum_heading(string name, class_heading * basetype) : enum_heading(name)
		{
			this->basetype = basetype;
		}

		string enum_heading::parse_string()
		{
			string rtn = "Enum(" + name;
			if (basetype != nullptr)
			{
				rtn += ", " + basetype->parse_string() + ")";
			}
			rtn += ")";
			return rtn;
		}

		modifier::modifier(modifier::modifier_type type, statement * modof) : modifier_base(modof)
		{
			this->type = type;
		}

		string modifier::parse_string()
		{
			switch (type)
			{
			case STATIC:
				return "StaticMod(" + on->parse_string() + ")";
			case PRIVATE:
				return "PrivateMod(" + on->parse_string() + ")";
			}
			return string();
		}

		throw_statement::throw_statement(statement * throws)
		{
			this->throws = throws;
		}

		string throw_statement::parse_string()
		{
			return "Throw(" + throws->parse_string() + ")";
		}

		modifier_base::modifier_base(statement * on)
		{
			this->on = on;
		}

		int modifier_base::indent_mod()
		{
			return on->indent_mod();
		}

		self_modifier::self_modifier(self_modifier::modifier_type type,
			self_modifier::modifier_time time, statement * on) : modifier_base(on)
		{
			this->type = type;
			this->time = time;
		}

		string self_modifier::parse_string()
		{
			string name;
			switch (time)
			{
			case PRE:
				name = "Pre";
				break;
			case POST:
				name = "Post";
				break;
			}
			switch (type)
			{
			case PLUS:
				name += "Increment";
				break;
			case MINUS:
				name += "Decriment";
				break;
			}
			return name + "(" + on->parse_string() + ")";
		}

		declaration_with_assignment::declaration_with_assignment(std::string name,
			type_statement type, statement * val)
		{
			this->name = name;
			this->type = type;
			this->value = val;
		}

		string declaration_with_assignment::parse_string()
		{
			return "DeclareAndAssign(" + name + ", " + type.parse_string() + ", " +
				value->parse_string() + ")";
		}



		import_statement::import_statement(string import_name)
		{
			this->import_name = import_name;
		}

		string import_statement::parse_string()
		{
			return "Import(" + import_name + ")";
		}

		list_accessor::list_accessor(statement * list, statement * index)
		{
			this->list = list;
			this->index = index;
		}

		string list_accessor::parse_string()
		{
			return "ListAccessor(" + list->parse_string() + ", " + index->parse_string() + ")";
		}

		compund_statement::compund_statement(vector<statement*> operations)
		{
			this->operations = operations;
		}

		string compund_statement::parse_string()
		{
			string ops;
			bool any = false;
			for (statement * stmt : operations)
			{
				ops += stmt->parse_string() + ",";
				any = true;
			}
			if (any)
			{
				ops = ops.substr(0, ops.length() - 1);
			}
			return "CompoundStatement(" + ops + ")";
		}

		else_heading::else_heading(block_heading * wrapping)
		{
			this->wrapping = wrapping;
		}

		string else_heading::parse_string()
		{
			if (wrapping == nullptr)
			{
				return "Else()";
			}
			return "Else(" + wrapping->parse_string() + ")";
		}

		void else_heading::finalize(std::stack<braced_block *> * stmts)
		{
			block_heading * if_head = (block_heading *) stmts->top()->get_last();
			if_head->add_else_block(this);
		}

		switch_heading::switch_heading(switch_heading::type typ, statement * on)
		{
			this->on = on;
			this->typ = typ;
		}

		string switch_heading::parse_string()
		{
			switch (typ)
			{
			case SWITCH:
				return "Switch(" + on->parse_string() + ")";
			case MATCH:
				return "Match(" + on->parse_string() + ")";
			}
			return "SwitchType(" + on->parse_string() + ")";
		}

		for_classic_heading::for_classic_heading(statement * init, statement * condition,
			statement * tick)
		{
			this->init = init;
			this->condition = condition;
			this->tick = tick;
		}

		string for_classic_heading::parse_string()
		{
			return "cFor(" + init->parse_string() + ", " + condition->parse_string() + ", " +
				tick->parse_string() + ")";
		}

		for_itterator_heading::for_itterator_heading(statement * val, statement * list)
		{
			this->val = val;
			this->list = list;
		}

		string for_itterator_heading::parse_string()
		{
			return "iFor(" + val->parse_string() + ", " + list->parse_string() + ")";
		}

		flow_statement::flow_statement(type typ)
		{
			this->typ = typ;
		}

		string flow_statement::parse_string()
		{
			switch (typ)
			{
			case BREAK:
				return "Break()";
			case NEXT:
				return "Next()";
			}
			return "UnknownFlow()";
		}

		switch_case_heading::switch_case_heading(statement * val)
		{
			this->val = val;
		}

		string switch_case_heading::parse_string()
		{
			return "SwitchCase(" + val->parse_string() + ")";
		}

		match_case_heading::match_case_heading(string name, type_statement t, 
			vector<string> struct_vals)
		{
			this->name = name;
			this->t = t;
			this->struct_vals = struct_vals;
		}

		string match_case_heading::parse_string()
		{
			string params;
			bool any = false;
			for (string v : struct_vals)
			{
				params += v + ",";
				any = true;
			}
			if (any)
			{
				params = params.substr(0, params.length() - 1);
			}
			return "MatchCase(" + name + " : " + t.parse_string() + ", Params(" + params + "))";
		}

		string try_heading::parse_string()
		{
			return "TryHeading()";
		}

		string finally_heading::parse_string()
		{
			return "FinallyHeading()";
		}

		catch_heading::catch_heading(statement * var)
		{
			this->var = var;
		}

		string catch_heading::parse_string()
		{
			return "CatchHeading(" + var->parse_string() + ")";
		}

		type_statement::type_statement(std::string name, std::vector<type_statement> generic_types)
		{
			this->name = name;
			this->generic_types = generic_types;
		}

		std::string type_statement::get_name()
		{
			return name;
		}

		std::string type_statement::parse_string()
		{
			if (generic_types.empty())
			{
				return "TypeClass(" + name + ")";
			}
			string params_rtn = "Generics(";
			bool any = false;
			for (type_statement tc : generic_types)
			{
				params_rtn += tc.parse_string() + ",";
				any = true;
			}
			if (any)
			{
				params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
			}
			return "TypeClass(" + name + ", " + params_rtn + "))";
		}

		skiff_class * type_statement::eval_class(scope * env)
		{
			return env->get_type(name);
		}
	}
}
