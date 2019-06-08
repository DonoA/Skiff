#pragma once

#include <stdbool.h>
#include <string.h>

typedef struct {
    long id;
    long size;
    void * data;
} skiff_list_t;