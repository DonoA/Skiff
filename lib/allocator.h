#pragma once

#include <stdlib.h>

#define STACK_SIZE 4096

void * skiff_ref_stack[STACK_SIZE/8] = { 0 };
size_t sp_ref = 0;

void * skalloc(size_t i, size_t size)
{
    return calloc(i, size);
}

void * skalloc_ref_stack() 
{
    void * data = skiff_ref_stack + sp_ref;
    sp_ref++;
    return data;
}

void skfree_ref_stack(size_t ref_len)
{
    sp_ref -= ref_len;
}
