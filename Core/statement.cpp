#include "stdafx.h"
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

		skiff_object statement::eval(scope * env)
		{
			return skiff_object();
		}

		string statement::parse_string()
		{
			return "Statement(" + raw + ")";
		}

		int statement::indent_mod()
		{
			return 0;
		}

		value::value(string val)
		{
			this->val = val;
		}

		skiff_object value::eval(scope * env)
		{
			return skiff_object();
		}

		//skiff_object value::eval(scope * env)
		//{
		//	return val;
		//}
		//
		//string value::eval_c()
		//{
		//	return *(string *) typ.get_scope()->get_function("to_string").eval(val)->get_value();
		//}

		string value::parse_string()
		{
			return "Value(" + val + ")";
		}

		decleration::decleration(string name, type_statement type)
		{
			this->name = name;
			this->type = type;
		}

		//skiff_object * decleration::eval(scope * env)
		//{
		//	env->define_variable(name, val->eval(env));
		//	return nullptr;
		//}
		//
		//string decleration::eval_c()
		//{
		//	return type.get_name() + " " + name + " = " + val->eval_c();
		//}

		string decleration::parse_string()
		{
			return "Decleration(" + name + "," + type.parse_string() + ")";
		}

		function_call::function_call(string name, vector<statement*> params)
		{
			this->name = utils::remove_pad(name);
			this->params = params;
		}

		//skiff_object * function_call::eval(scope * env)
		//{
		//	if (name == "print")
		//	{
		//		string tp;
		//		for (statement * stmt : params)
		//		{
		//			skiff_object * res = stmt->eval(env);
		//			res = res->get_type().get_scope()->get_function("to_string").eval(res);
		//			tp += *((string *) res->get_value());
		//			tp += " ";
		//		}
		//		tp.erase(tp.length() - 1);
		//		std::cout << tp << std::endl;
		//	}
		//	return nullptr;
		//}
		//
		//string function_call::eval_c()
		//{
		//	string rtn = name + "(";
		//	for (statement * stmt : params)
		//	{
		//		rtn += stmt->eval_c() + ",";
		//	}
		//	rtn = rtn.substr(0, rtn.length() - 1);
		//	rtn += ");";
		//	return rtn;
		//}

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

		//skiff_object * variable::eval(scope * env)
		//{
		//	return env->get_variable(name);
		//}
		//
		//string variable::eval_c()
		//{
		//	return name;
		//}

		string variable::parse_string()
		{
			return "Variable(" + name + ")";
		}

		assignment::assignment(statement * name, statement * value)
		{
			this->name = name;
			this->val = value;
		}

		//skiff_object * assignment::eval(scope * env)
		//{
		//	skiff_object * v = val->eval(env);
		//	env->define_variable(name, v);
		//	return v;
		//}
		//
		//string assignment::eval_c()
		//{
		//	return name + "=" + val->eval_c();
		//}

		string assignment::parse_string()
		{
			return "Assignment(" + name->parse_string() + "," + val->parse_string() + ")";
		}

		math_statement::math_statement(queue<statement*> operands, queue<char> operators)
		{
			this->operands = operands;
			this->operators = operators;
		}

		//skiff_object * math_statement::eval(scope * env)
		//{
		//	queue<char> ops = operators;
		//	queue<statement *> stmts = operands;
		//	skiff_object * base = stmts.front()->eval(env);
		//	skiff_object * t = base->get_type().get_scope()->get_function("clone").eval(base);
		//	stmts.pop();
		//	while (!ops.empty())
		//	{
		//		math_statement::eval_single_op(t, ops.front(), stmts.front()->eval(env));
		//		stmts.pop();
		//		ops.pop();
		//	}
		//	return t;
		//}
		//
		//string math_statement::eval_c()
		//{
		//	string rtn = string();
		//	queue<char> ops = operators;
		//	queue<statement *> stmts = operands;
		//	while (!ops.empty())
		//	{
		//		rtn += stmts.front()->eval_c() + " " + ops.front() + " ";
		//		stmts.pop();
		//		ops.pop();
		//	}
		//	rtn += stmts.front()->eval_c();
		//	return rtn;
		//}

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

		void math_statement::eval_single_op(skiff_object s1, char op, skiff_object s2)
		{
			//skiff_object * o = get_dominant_type(&s1, &s2);
			//if (o == nullptr)
			//{
			//	o = &s1;
			//}
			//vector<skiff_object> p;
			//p.push_back(s2);
			//(*o->get_type().get_operators())[string(1, op)].eval(*o, p);
		}

		comparison::comparison(statement * s1, comparison::comparison_type typ, statement * s2)
		{
			this->s1 = s1;
			this->s2 = s2;
			this->typ = typ;
		}

		//skiff_object * comparison::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string comparison::eval_c()
		//{
		//	return s1->eval_c() + " " + this->comparison_string() + " " + s2->eval_c();
		//}

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

		//skiff_object * invert::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string invert::eval_c()
		//{
		//	return "!" + val->eval_c();
		//}

		string invert::parse_string()
		{
			return "Invert(" + val->parse_string() + ")";
		}

		bitinvert::bitinvert(statement * value)
		{
			this->val = value;
		}

		//skiff_object * bitinvert::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string bitinvert::eval_c()
		//{
		//	return "~" + val->eval_c();
		//}

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

		//skiff_object * bitwise::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string bitwise::eval_c()
		//{
		//	return string();
		//}

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

		//skiff_object * boolean_conjunction::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string boolean_conjunction::eval_c()
		//{
		//	return s1->eval_c() + " " + this->conj_string() + " " + s2->eval_c();
		//}

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

		if_heading::if_heading(statement * condition)
		{
			this->condition = condition;
		}

		//skiff_object * if_heading::eval(scope * env)
		//{
		//	return condition->eval(env);
		//}
		//
		//string if_heading::eval_c()
		//{
		//	return "if(" + condition->eval_c() + ")";
		//}

		string if_heading::parse_string()
		{
			return "If(" + condition->parse_string() + ")";
		}

		//skiff_object * class_heading::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string class_heading::eval_c()
		//{
		//	return "NOT CONVERTABLE";
		//}

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

		//skiff_object * function_heading::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string function_heading::eval_c()
		//{
		//	string params_rtn = string();
		//	bool any = false;
		//	for (function::function_parameter p : params)
		//	{
		//		params_rtn += function_parameter_c_sig(p) + ",";
		//		any = true;
		//	}
		//	if (any)
		//	{
		//		params_rtn = params_rtn.substr(0, params_rtn.length() - 1);
		//	}
		//	return returns.get_name() + " " + name + "(" + params_rtn + ")";
		//}

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

		//skiff_object * while_heading::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string while_heading::eval_c()
		//{
		//	return "while(" + condition->eval_c() + ")";
		//}

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

		//skiff_object * return_statement::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string return_statement::eval_c()
		//{
		//	return "return " + returns->eval_c();
		//}

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

		//skiff_object * new_skiff_object_statement::eval(scope * env)
		//{
		//	return nullptr;
		//}
		//
		//string new_skiff_object_statement::eval_c()
		//{
		//	return "NOT CONVERTABLE";
		//}

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

		decleration_with_assignment::decleration_with_assignment(std::string name,
			type_statement type, statement * val)
		{
			this->name = name;
			this->type = type;
			this->value = val;
		}

		string decleration_with_assignment::parse_string()
		{
			return "DeclareAndAssign(" + name + ", " + type.parse_string() + ", " +
				value->parse_string() + ")";
		}

		environment::skiff_object decleration_with_assignment::eval(environment::scope * env)
		{
			skiff_class clazz = type.eval_class(env);
			vector<skiff_object> params = {
				value->eval(env)
			};
			env->define_variable(name, clazz.construct(params));
			return environment::skiff_object();
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
		skiff_class type_statement::eval_class(scope * env)
		{
			return env->get_type(name);
		}
}
}
