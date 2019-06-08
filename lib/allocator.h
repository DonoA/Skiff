#pragma once

#include <stdlib.h>

#define STACK_SIZE 4096

void * allMem[STACK_SIZE] = { 0 };
size_t currentMem = 0;

void * skalloc(size_t count, size_t size) 
{
    return calloc(count, size);
}
