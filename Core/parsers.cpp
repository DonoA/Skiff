#include "stdafx.h"
#include "parsers.h"


namespace skiff
{
	using ::std::vector;
	using ::std::string;
	using ::std::queue;
	using ::std::stack;

	using ::skiff::types::type_class;

	vector<statements::statement *> parse_argument_statements(vector<string> params)
	{
		vector<statements::statement *> parsed;
		for (string s : params)
		{
			parsed.push_back(parse_statement(s));
		}
		return parsed;
	}

	type_class box_type(string stmt)
	{
		if (!stmt.empty() && stmt.find_first_not_of("0123456789") == std::string::npos)
		{
			return type_class("Int");
		}
		else if (!stmt.empty() && stmt.find_first_not_of("0123456789.") == std::string::npos)
		{
			return type_class("Double");
		}
		else if (!stmt.empty() && stmt[0] == '"' && stmt[stmt.length() - 1] == '"')
		{
			return type_class("String");
		}
		else if (!stmt.empty() && stmt[0] == '\'' && stmt[stmt.length() - 1] == '\'')
		{
			return type_class("Sequence");
		}
		else if (!stmt.empty() && (stmt == "true" || stmt == "false"))
		{
			return type_class("Boolean");
		}
		return type_class("Var");
	}

	type_class parse_type_class_name(string name)
	{
		size_t p = name.find_first_of('<');
		if (p == string::npos)
		{
			return type_class(name);
		}
		string g = name.substr(p + 1, name.find_last_of('>') - (p + 1));
		vector<string> gv = utils::string_split(g, ",");
		vector<type_class> gvt;
		for (string s : gv)
		{
			gvt.push_back(type_class(utils::remove_pad(s)));
		}
		return type_class(name.substr(0, p), gvt);
	}

	bool is_math(char c)
	{
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '%';
	}

	statements::math_statement * parse_math(string stmt)
	{
		stack<char> braces;
		queue<char> operators;
		queue<statements::statement *> operands;
		int j = 0;
		for (unsigned i = 0; i < stmt.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
			if (is_math(stmt[i]) && braces.empty())
			{
				operands.push(parse_statement(stmt.substr(j, i - j)));
				operators.push(stmt[i]);
				j = i + 1;
			}
		}
		if (stmt != "")
		{
			operands.push(parse_statement(stmt.substr(j)));
		}
		return new statements::math_statement(operands, operators);
	}

	statements::function_heading * parse_function_header(string stmt)
	{
		size_t j, i = j = stmt.find_first_of("(");
		string dec = utils::string_split(stmt.substr(0, i), " ")[1];
		string params;
		stack<char> braces;
		for (; i < stmt.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
			if (stmt[i] == ')' && braces.empty())
			{
				params = stmt.substr(j + 1, i - j - 1);
				break;
			}
		}
		string returns = stmt.substr(i + 1);
		returns.erase(0, returns.find_first_of(":") + 1);
		returns = utils::remove_pad(returns);
		vector<string> params_list = utils::braced_split(params, ',');
		vector<types::function::function_parameter> f_params;
		for (string s : params_list)
		{
			vector<string> n = utils::string_split(s, ":");
			f_params.push_back(types::function::create_function_parameter(utils::remove_pad(n[0]),
				parse_type_class_name(utils::remove_pad(n[1]))));
		}
		return new statements::function_heading(dec, f_params, parse_type_class_name(returns));
	}

	statements::new_object_statement * parse_object_creation(string stmt)
	{
		size_t j, i = j = stmt.find_first_of("(");
		string dec = stmt.substr(0, i);
		string params_str;
		stack<char> braces;
		for (; i < stmt.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
			if (stmt[i] == ')' && braces.empty())
			{
				params_str = stmt.substr(j + 1, i - j - 1);
				break;
			}
		}
		vector<statements::statement *> params = parse_argument_statements(utils::braced_split(params_str, ','));
		return new statements::new_object_statement(parse_type_class_name(dec), params);
	}

	statements::annotation_tag * parse_annotation_tag(string tag)
	{
		size_t i = tag.find_first_of('(');
		if (i == string::npos)
		{
			size_t split = std::min(tag.find_first_of(' '), tag.find_first_of('\n'));
			string on = utils::remove_pad(tag.substr(split + 1));
			return new statements::annotation_tag(tag.substr(0, split), 
				vector<statements::statement *>(),
				parse_statement(on));
		}
		if (i > tag.find_first_of('\n'))
		{
			size_t split = tag.find_first_of('\n');
			string on = utils::remove_pad(tag.substr(split + 1));
			return new statements::annotation_tag(tag.substr(0, split), 
				vector<statements::statement *>(),
				parse_statement(on));
		}
		string name = tag.substr(0, i);
		size_t j = i = tag.find_first_of("(");
		string params;
		stack<char> braces;
		for (; i < tag.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : tag[i - 1], tag[i], &braces);
			if (tag[i] == ')' && braces.empty())
			{
				params = tag.substr(j + 1, i - j - 1);
				break;
			}
		}
		vector<statements::statement *> p = 
			parse_argument_statements(utils::braced_split(params, ','));
		string on = tag.substr(i + 1);
		return new statements::annotation_tag(name, p, parse_statement(on));
	}

	statements::class_heading * parse_class_heading(statements::class_heading::class_type type, 
		string stmt)
	{
		size_t p = stmt.find_first_of('<');
		size_t c = stmt.find_last_of(':');
		if (p == string::npos && c == string::npos)
		{
			return new statements::class_heading(type, stmt);
		}
		vector<statements::class_heading::heading_generic> gvt;
		string c_name;
		type_class extends = type_class("");
		if (p != string::npos)
		{
			string g = stmt.substr(p + 1, stmt.find_last_of('>') - (p + 1));
			vector<string> gv = utils::string_split(g, ",");
			c_name = stmt.substr(0, p);
			for (string s : gv)
			{
				vector<string> bts = utils::string_split(s, ":");
				if (bts.size() == 1)
				{
					gvt.push_back(statements::class_heading::generate_generic_heading(
						utils::remove_pad(bts[0]),
						type_class("")));
				}
				else
				{
					gvt.push_back(statements::class_heading::generate_generic_heading(
						utils::remove_pad(bts[0]),
						type_class(utils::remove_pad(bts[1]))));
				}
			}
		}
		if (c != string::npos)
		{
			if (p == string::npos)
			{
				vector<string> gv = utils::string_split(stmt, ":");
				c_name = utils::remove_pad(gv[0]);
				extends = type_class(utils::remove_pad(gv[1]));
			}
			else if (c > p)
			{
				extends = type_class(utils::remove_pad(stmt.substr(c + 1)));
			}
		}
		return new statements::class_heading(type, c_name, gvt, extends);
	}

	statements::enum_heading * parse_enum_heading(string stmt)
	{
		vector<string> s = utils::string_split(stmt, " ");
		if (s[0] == "struct")
		{
			statements::class_heading * heading = parse_class_heading(
				statements::class_heading::class_type::STRUCT, s[1]);
			return new statements::enum_heading(heading->get_name(), heading);
		}
		else if (s[0] == "class")
		{
			statements::class_heading * heading = parse_class_heading(
				statements::class_heading::class_type::CLASS, s[1]);
			return new statements::enum_heading(heading->get_name(), heading);
		}
		return new statements::enum_heading(stmt);
	}

	statements::statement * parse_decleration(string name, string other)
	{
		size_t eq_i = other.find_first_of('=');
		if (eq_i == string::npos)
		{
			return new statements::decleration(name, parse_type_class_name(other));
		}
		else
		{
			string typ = utils::remove_pad(other.substr(0, eq_i));
			string val = utils::remove_pad(other.substr(eq_i + 1));
			return new statements::decleration_with_assignment(parse_statement(name),
				parse_type_class_name(typ), parse_statement(val));
		}
	}

	size_t scroll_to_next_close_brace(string stmt, size_t ci)
	{
		stack<char> braces;
		for (size_t j = ci; j < stmt.length(); j++)
		{
			if (braces.empty() && stmt[j] == ')')
			{
				return j;
			}
			utils::track_braces(j == 0 ? '\0' : stmt[j - 1], stmt[j], &braces);
		}
		return stmt.length();
	}

	statements::statement * parse_for_heading(string stmt)
	{
		vector<string> s = utils::braced_split(stmt, ';');
		if (s.size() == 3)
		{
			vector<statements::statement *> ss = parse_argument_statements(s);
			return new statements::for_classic_heading(ss[0], ss[1], ss[2]);
		}
		size_t sp = stmt.find_last_of(":");
		string p1 = stmt.substr(0, sp);
		string p2 = stmt.substr(sp + 1);
		return new statements::for_itterator_heading(parse_statement(p1), parse_statement(p2));
	}

	statements::statement * parse_case_statement(string stmt)
	{
		string c = utils::remove_pad(stmt.substr(0, stmt.find_last_of("=>") - 1));
		size_t s = c.find_first_of(":");
		if (s == string::npos)
		{
			return new statements::switch_case_heading(parse_statement(c));
		}
		string p1 = utils::remove_pad(c.substr(0, s));
		string p2 = utils::remove_pad(c.substr(s + 1));
		s = p2.find_first_of("(");
		if (s == string::npos)
		{
			return new statements::match_case_heading(p1, parse_type_class_name(p2));
		}
		string c_name = p2.substr(0, s);
		vector<string> p_names = 
			utils::braced_split(p2.substr(s + 1, p2.find_last_of(")") - (s + 1)), ',');
		return new statements::match_case_heading(p1, parse_type_class_name(c_name), p_names);
	}

	statements::statement * scan_for_keyword(string stmt)
	{
		stack<char> braces;
		string p1, p2;
		if (stmt == "else")
		{
			return new statements::else_heading();
		}
		else if (stmt == "try")
		{
			return new statements::try_heading();
		}
		else if (stmt == "finally")
		{
			return new statements::finally_heading();
		}
		else if (stmt == "break")
		{
			return new statements::flow_statement(statements::flow_statement::type::BREAK);
		}
		else if (stmt == "next")
		{
			return new statements::flow_statement(statements::flow_statement::type::NEXT);
		}
		for (size_t i = 0; i < stmt.length(); i++)
		{
			if (braces.empty())
			{
				if (stmt[i] == ' ')
				{
					p1 = utils::remove_pad(stmt.substr(0, i));
					p2 = utils::remove_pad(stmt.substr(i + 1));
					if (p1 == "new")
					{
						return parse_object_creation(p2);
					}
					else if (p1 == "def")
					{
						return parse_function_header(stmt);
					}
					else if (p1 == "class")
					{
						return parse_class_heading(statements::class_heading::class_type::CLASS, p2);
					}
					else if (p1 == "struct")
					{
						return parse_class_heading(statements::class_heading::class_type::STRUCT, p2);
					}
					else if (p1 == "enum")
					{
						return parse_enum_heading(p2);
					}
					else if (p1 == "static")
					{
						return new statements::modifier(statements::modifier::modifier_type::STATIC, 
							parse_statement(p2));
					}
					else if (p1 == "private")
					{
						return new statements::modifier(statements::modifier::modifier_type::PRIVATE,
							parse_statement(p2));
					}
					else if (p1 == "case")
					{
						return parse_case_statement(p2);
					}
					else if (p1 == "throw")
					{
						return new statements::throw_statement(parse_statement(p2));
					}
					else if (p1 == "return")
					{
						return new statements::return_statement(parse_statement(p2));
					}
					else if (p1 == "annotation")
					{
						return parse_class_heading(statements::class_heading::class_type::ANNOTATION, p2);
					}
					else if (p1 == "import")
					{
						return new statements::import_statement(p2);
					}
					else if (p1 == "else")
					{
						return new statements::else_heading(
							(statements::block_heading *)scan_for_keyword(p2));
					}
				}
				else if (stmt[i] == ':')
				{
					p1 = utils::remove_pad(stmt.substr(0, i));
					p2 = utils::remove_pad(stmt.substr(i + 1));
					return parse_decleration(p1, p2);
				}
				else if (stmt[i] == '=')
				{
					if (stmt[i + 1] == '=' || stmt[i - 1] == '=' || stmt[i - 1] == '>' ||
						stmt[i - 1] == '<' || stmt[i - 1] == '!' || is_math(stmt[i - 1]))
					{
						continue;
					}
					p1 = stmt.substr(0, i);
					p2 = stmt.substr(i + 1);
					return new statements::assignment(parse_statement(utils::remove_pad(p1)), 
						parse_statement(p2));
				}
				else if (stmt[i] == '@')
				{
					p1 = utils::remove_pad(stmt.substr(1));
					return parse_annotation_tag(p1);
				}
				else if (stmt[i] == '(')
				{
					p1 = utils::remove_pad(stmt.substr(0, i));
					p2 = stmt.substr(i + 1, stmt.find_last_of(')') - (i + 1));
					if (p1 == "if")
					{
						return new statements::if_heading(parse_statement(p2));
					}
					else if (p1 == "while")
					{
						return new statements::while_heading(parse_statement(p2));
					}
					else if (p1 == "for")
					{
						return parse_for_heading(p2);
					}
					else if (p1 == "switch")
					{
						return new statements::switch_heading(
							statements::switch_heading::type::SWITCH, parse_statement(p2));
					}
					else if (p1 == "match")
					{
						return new statements::switch_heading(
							statements::switch_heading::type::MATCH, parse_statement(p2));
					}
					else if (p1 == "catch")
					{
						return new statements::catch_heading(parse_statement(p2));
					}
					else if (p1 == "")
					{
						// analyse what is in the parens (math or first class function)
					}
				}
			}
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
		}
		return nullptr;
	}

	statements::statement * scan_for_and(string stmt)
	{
		stack<char> braces;
		string p1, p2;
		for (size_t i = 0; i < stmt.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
			if (braces.empty() && stmt[i] == '&' && stmt[i + 1] == '&')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 2);
				statements::comparison * c1 = (statements::comparison *)parse_statement(p1);
				statements::comparison * c2 = (statements::comparison *)parse_statement(p2);
				return new statements::boolean_conjunction(c1, 
					statements::boolean_conjunction::conjunction_type::And, c2);
			}
		}
		return nullptr;
	}

	statements::statement * scan_for_or(string stmt)
	{
		stack<char> braces;
		string p1, p2;
		for (size_t i = 0; i < stmt.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
			if (braces.empty() && stmt[i] == '|' && stmt[i + 1] == '|')
			{
				p1 = stmt.substr(0, i);
				p2 = stmt.substr(i + 2);
				statements::comparison * c1 = (statements::comparison *)parse_statement(p1);
				statements::comparison * c2 = (statements::comparison *)parse_statement(p2);
				return new statements::boolean_conjunction(c1,
					statements::boolean_conjunction::conjunction_type::Or, c2);
			}
		}
		return nullptr;
	}

	statements::statement * scan_for_camparison(string stmt)
	{
		stack<char> braces;
		string p1, p2;
		for (size_t i = 0; i < stmt.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
			if (braces.empty() && (stmt[i] == '<' || stmt[i] == '>' || (stmt[i] == '!' &&
				stmt[i + 1] == '=') || stmt[i] == '='))
			{
				p1 = utils::remove_pad(stmt.substr(0, i));
				p2 = utils::remove_pad(stmt.substr(i + 1));
				statements::comparison::comparison_type typ;
				if (stmt[i] == '<' && stmt[i - 1] != '<' && stmt[i + 1] != '<')
				{
					typ = statements::comparison::comparison_type::LessThan;
					if (stmt[i + 1] == '=')
					{
						typ = statements::comparison::comparison_type::LessThanEqualTo;
						p2 = utils::remove_pad(stmt.substr(i + 2));
					}
					return new statements::comparison(parse_statement(p1), typ, parse_statement(p2));
				}
				else if (stmt[i] == '>' && stmt[i - 1] != '>' && stmt[i + 1] != '>')
				{
					typ = statements::comparison::comparison_type::GreaterThan;
					if (stmt[i + 1] == '=')
					{
						typ = statements::comparison::comparison_type::GreaterThanEqualTo;
						p2 = utils::remove_pad(stmt.substr(i + 2));
					}
					return new statements::comparison(parse_statement(p1), typ, parse_statement(p2));
				}
				else if (stmt[i] == '!')
				{
					typ = statements::comparison::comparison_type::NotEqual;
					p2 = utils::remove_pad(stmt.substr(i + 2));
					return new statements::comparison(parse_statement(p1), typ, parse_statement(p2));
				}
				else if (stmt[i] == '=' && stmt[i + 1] == '=')
				{
					typ = statements::comparison::comparison_type::Equal;
					p2 = utils::remove_pad(stmt.substr(i + 2));
					return new statements::comparison(parse_statement(p1), typ, parse_statement(p2));
				}
			}
		}
		return nullptr;
	}

	statements::statement * scan_for_math(string stmt)
	{
		stack<char> braces;
		string p1, p2;
		for (size_t i = 0; i < stmt.length(); i++)
		{
			utils::track_braces(i == 0 ? '\0' : stmt[i - 1], stmt[i], &braces);
			if (braces.empty())
			{
				if (is_math(stmt[i]))
				{
					if (i + 1 < stmt.length() && stmt[i + 1] == '=')
					{
						p1 = utils::remove_pad(stmt.substr(0, i));
						p2 = stmt.substr(i + 2);
						queue<char> ops;
						ops.push(stmt[i]);
						queue<statements::statement *> stmts;
						stmts.push(parse_statement(p1));
						stmts.push(parse_statement(p2));
						return new statements::assignment(parse_statement(p1), 
							new statements::math_statement(stmts, ops));
					}
					if (stmt[i] == '+' && i + 1 < stmt.length() && stmt[i + 1] == '+')
					{
						p1 = utils::remove_pad(stmt.substr(0, i));
						p2 = stmt.substr(i + 2);
						if (p1 == "")
						{
							return new statements::self_modifier(
								statements::self_modifier::modifier_type::PLUS,
								statements::self_modifier::modifier_time::PRE, parse_statement(p2));
						}
						if (p2 == "")
						{
							return new statements::self_modifier(
								statements::self_modifier::modifier_type::PLUS,
								statements::self_modifier::modifier_time::POST, parse_statement(p1));
						}
					}
					if (stmt[i] == '-' && i + 1 < stmt.length() && stmt[i + 1] == '-')
					{
						p1 = utils::remove_pad(stmt.substr(0, i));
						p2 = stmt.substr(i + 2);
						if (p1 == "")
						{
							return new statements::self_modifier(
								statements::self_modifier::modifier_type::MINUS,
								statements::self_modifier::modifier_time::PRE, parse_statement(p2));
						}
						if (p2 == "")
						{
							return new statements::self_modifier(
								statements::self_modifier::modifier_type::MINUS,
								statements::self_modifier::modifier_time::POST, parse_statement(p1));
						}
					}
					return parse_math(stmt);
				}
				else if (stmt[i] == '&' || stmt[i] == '|')
				{
					p1 = stmt.substr(0, i);
					p2 = stmt.substr(i + 1);
					statements::statement * s1 = parse_statement(p1);
					statements::statement * s2 = parse_statement(p2);
					statements::bitwise::operation op = statements::bitwise::operation::And;
					if (stmt[i] == '|')
					{
						op = statements::bitwise::operation::Or;
					}
					return new statements::bitwise(s1, op, s2);
				}
				else if (stmt[i] == '^')
				{
					p1 = stmt.substr(0, i);
					p2 = stmt.substr(i + 1);
					statements::statement * s1 = parse_statement(p1);
					statements::statement * s2 = parse_statement(p2);
					statements::bitwise::operation op = statements::bitwise::operation::Xor;
					return new statements::bitwise(s1, op, s2);
				}
				else if (stmt[i] == '>' && stmt[i + 1] == '>')
				{
					p1 = stmt.substr(0, i);
					p2 = stmt.substr(i + 2);
					statements::statement * s1 = parse_statement(p1);
					statements::statement * s2 = parse_statement(p2);
					statements::bitwise::operation op = statements::bitwise::operation::ShiftRight;
					return new statements::bitwise(s1, op, s2);
				}
				else if (stmt[i] == '<' && stmt[i + 1] == '<')
				{
					p1 = stmt.substr(0, i);
					p2 = stmt.substr(i + 2);
					statements::statement * s1 = parse_statement(p1);
					statements::statement * s2 = parse_statement(p2);
					statements::bitwise::operation op = statements::bitwise::operation::ShiftLeft;
					return new statements::bitwise(s1, op, s2);
				}
			}
		}
		return nullptr;
	}

	statements::statement * scan_for_doted(string stmt)
	{
		stack<char> braces;
		vector<string> stmts = utils::braced_split(stmt, '.');
		for (string s : stmts)
		{
			if (box_type(s).get_name() == "Int")
			{
				return nullptr;
			}
		}
		if (stmts.size() > 1)
		{
			return new statements::compund_statement(parse_argument_statements(stmts));
		}
		return nullptr;
	}

	statements::statement * scan_for_remaining(string stmt)
	{
		string p1, p2;
		bool parsing_string = false;
		for (size_t i = 0; i < stmt.length(); i++)
		{
			if ((stmt[i] == '"' || stmt[i] == '\'') && (i == 0 || stmt[i - 1] != '\\'))
			{
				parsing_string = !parsing_string;
			}
			if (!parsing_string)
			{
				if (stmt[i] == '!')
				{
					if (stmt[i + 1] == '=')
					{
						continue;
					}
					p2 = utils::remove_pad(stmt.substr(i + 1));
					return new statements::invert(parse_statement(p2));
				}
				else if (stmt[i] == '~')
				{
					p2 = utils::remove_pad(stmt.substr(i + 1));
					return new statements::bitinvert(parse_statement(p2));
				}
				else if (stmt[i] == '(')
				{
					p1 = utils::remove_pad(stmt.substr(0, i));
					p2 = stmt.substr(i + 1, scroll_to_next_close_brace(stmt, i + 1) - (i + 1));
					if (p1 == "")
					{
						return parse_statement(p2);
					}
					vector<string> params = utils::braced_split(p2, ',');
					vector<statements::statement *> param_stmt;
					for (string s : params)
					{
						param_stmt.push_back(parse_statement(s));
					}
					return new statements::function_call(p1, param_stmt);
				}
				else if (stmt[i] == '[')
				{
					p1 = utils::remove_pad(stmt.substr(0, i));
					p2 = stmt.substr(i + 1, stmt.find_last_of(']') - (i + 1));
					statements::statement * accessor = parse_statement(p2);
					return new statements::list_accessor(parse_statement(p1), accessor);
				}
			}
		}
		return nullptr;
	}

	statements::statement * parse_statement(string stmt)
	{
		stmt = utils::remove_pad(stmt);
		// TODO: refactor this to use an array of string possitions that are ranked. This
		// way only one string pass is needed but the most powerful deimetter can be used.

		statements::statement * parsed;

		parsed = scan_for_keyword(stmt);
		if (parsed != nullptr)
		{
			return parsed;
		}

		parsed = scan_for_and(stmt);
		if (parsed != nullptr)
		{
			return parsed;
		}

		parsed = scan_for_or(stmt);
		if (parsed != nullptr)
		{
			return parsed;
		}

		parsed = scan_for_camparison(stmt);
		if (parsed != nullptr)
		{
			return parsed;
		}

		parsed = scan_for_math(stmt);
		if (parsed != nullptr)
		{
			return parsed;
		}

		parsed = scan_for_doted(stmt);
		if (parsed != nullptr)
		{
			return parsed;
		}

		parsed = scan_for_remaining(stmt);
		if (parsed != nullptr)
		{
			return parsed;
		}

		stmt = utils::remove_pad(stmt);
		if (stmt == "")
		{
			return new statements::statement(stmt);
		}
		if (box_type(stmt).get_name() != "Var")
		{
			return new statements::value(stmt);
		}
		//return new variable(stmt);
		//std::cerr << "Could not locate refrence: '" << stmt << "'" << std::endl;
		return new statements::statement(stmt);
	}

	bool handle_line(string input, char c, queue<statements::statement *> * stmts)
	{
		input = utils::remove_pad(input);
		if (input != "")
		{
			statements::statement * stmt = parse_statement(input);
			stmts->push(stmt);
		}
		if (c == '}')
		{
			stmts->push(new statements::end_block_statement());
		}
		//if (c == '}')
		//{
		//	stmts->push(new end_block_statement());
		//}
		if (input == "exit()")
		{
			return false;
		}
		return true;
	}
}