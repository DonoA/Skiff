#include <stdlib.h>

#include "skiff_string.h"

skiff_string_t * skiff_float_to_string(float i)
{
    char buf[13] = { 0 };
    sprintf(buf, "%f", i);
    skiff_string_t * str = skiff_string_new_0(0, buf);
    return str;
}