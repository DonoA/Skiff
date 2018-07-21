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

        string value::parse_string()
        {
            return "Value(" + val + ")";
        }

        declaration::declaration(string name, type_statement type)
        {
            this->name = name;
            this->type = type;
        }

        string declaration::parse_string()
        {
            return "Decleration(" + name + "," + type.parse_string() + ")";
        }

        function_call::function_call(statement * name, vector<statement*> params)
        {
            this->name = name;
            this->params = params;
        }

        string function_call::parse_string()
        {
            string rtn = "FunctionCall(" + name->parse_string() + ", Params(";
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

        string math_statement::parse_string()
        {
            string rtn = "MathStatement(";
            return "MathStatement(" + statement1->parse_string() + " " +
                   std::to_string((int) opr) + " " +
                   statement2->parse_string() + ")";
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
            case EQUAL:
                return "==";
            case NOT_EQUAL:
                return "!=";
            case LESS_THAN:
                return "<";
            case LESS_THAN_EQUAL_TO:
                return "<=";
            case GREATER_THAN:
                return ">";
            case GREATER_THAN_EQUAL_TO:
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
            case AND:
                return "&";
            case OR:
                return "|";
            case XOR:
                return "^";
            case SHIFT_LEFT:
                return "<<";
            case SHIFT_RIGHT:
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
            case AND:
                return "&&";
            case OR:
                return "||";
            }
            return "";
        }

        string block_heading::eval_c()
        {
            return string();
        }

        skiff_object block_heading::eval(scope * env)
        {
            return skiff_object();
        }

        string if_directive::parse_string()
        {
            return "If(" + condition->parse_string() + ")";
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


        string function_definition::parse_string()
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
            string heading = "FunctionHeading(" + name + ", " + params_rtn +
                             ", Returns(" + returns.parse_string() + "))";
            string bdy = "{\n";
            for(statement * s : body)
            {
                bdy += s->parse_string() + "\n";
            }
            bdy += "}";
            return heading + bdy;
        }

        string function_definition::function_parameter_sig(function_parameter p)
        {
            return "Param(" + p.name + +"," + p.typ.parse_string() + ")";
        }

        string function_definition::function_parameter_c_sig(function_parameter p)
        {
            //builtin::type t = builtin::get_type_for(p.typ.get_class_id());
            //return builtin::get_c_type_for(t) + " " + p.name;
            return string();
        }

        string while_directive::parse_string()
        {
            return "While(" + condition->parse_string() + ")";
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

        annotation_tag::annotation_tag(string tag_name, vector<statement *> params) : modifier_base(nullptr)
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
            return "Annotation(" + name + ", Params(" + parms + "))";
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

        string else_directive::parse_string()
        {
            return "Else()";
        }

        string switch_directive::parse_string()
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

        string for_classic_directive::parse_string()
        {
            return "cFor(" + init->parse_string() + ", " + condition->parse_string() + ", " +
                tick->parse_string() + ")";
        }

        string for_itterator_directive::parse_string()
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

        string switch_case_directive::parse_string()
        {
            return "SwitchCase(" + val->parse_string() + ")";
        }

        string match_case_directive::parse_string()
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

        string try_directive::parse_string()
        {
            return "TryHeading()";
        }

        string finally_directive::parse_string()
        {
            return "FinallyHeading()";
        }

        string catch_directive::parse_string()
        {
            return "CatchHeading(" + var->parse_string() + ")";
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
