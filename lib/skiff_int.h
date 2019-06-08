#pragma once
#include "allocator.h"

int32_t * skiff_int_new(int32_t value)
{
    int32_t * v = (int32_t *) skalloc(1, sizeof(int32_t));
    *v = value;
    return v;
}