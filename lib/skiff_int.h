#pragma once
#include "allocator.h"

int32_t * skiff_int_new(int32_t value)
{
    int32_t * v = (int32_t *) skalloc_data_stack(sizeof(int32_t));
    *v = value;
    return v;
}

int32_t * skiff_int_mul(int32_t * s1, int32_t * s2)
{
    int32_t * res = (int32_t *) skalloc_data_stack(sizeof(int32_t));
    *res = (*s1) * (*s2);
    return res;
}

int32_t * skiff_int_add(int32_t * s1, int32_t * s2)
{
    int32_t * res = (int32_t *) skalloc_data_stack(sizeof(int32_t));
    *res = (*s1) + (*s2);
    return res;
}