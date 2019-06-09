#pragma once

#include <stdlib.h>

#define STACK_SIZE 4096

uint8_t skiff_data_stack[STACK_SIZE] = { 0 };
void * skiff_ref_stack[STACK_SIZE/8] = { 0 };
size_t sp_data = 0;
size_t sp_ref = 0;

void * skalloc_data_stack(size_t len) 
{
    void * data = skiff_data_stack + sp_data;
    sp_data += len;
    return data;
}

void * skalloc_ref_stack() 
{
    void * data = skiff_ref_stack + sp_ref;
    sp_ref++;
    return data;
}

void ** skalloc_heap(size_t count, size_t size) 
{
    void ** ptr = skalloc_ref_stack();
    *ptr = calloc(count, size);
    return ptr;
}

void skiff_gc_clean(size_t data_len, size_t ref_len)
{
    sp_data -= data_len;
    sp_ref -= ref_len;
}
