#pragma once

#include <stdlib.h>
#include <stdio.h>
#include "skiff_anyref.h"
#include "skiff_string.h"

#define STACK_SIZE 4096
#define HEAP_SIZE 4096
#define REF_SIZE 8

#define CLEAN 0
#define MARKED 1

skiff_any_ref_t * skiff_ref_stack[STACK_SIZE/REF_SIZE] = { 0 };
uint8_t * heap_space;
size_t sp_ref = 0;
size_t heap_ref = 0;

void * skalloc(size_t i, size_t size)
{
    // printf("Calloc size=%li\n", size);
    uint8_t * data = heap_space + heap_ref;
    // printf("data %p\n", heap_space);
    heap_ref += size;
    memset(data, 0, size);
    return data;
}

void * skalloc_ref_stack() 
{
    // printf("sp_ref=%li\n", sp_ref);
    skiff_any_ref_t ** data = skiff_ref_stack + sp_ref;
    sp_ref++;
    return data;
}

void skfree_ref_stack(size_t ref_len)
{
    // printf("sp_ref=%li\n", sp_ref);
    sp_ref -= ref_len;
}

void skfree_set_ref_stack(size_t ref_len)
{
    // printf("sp_ref=%li\n", sp_ref);
    sp_ref = ref_len;
}

void skiff_allocator_init()
{
    heap_space = calloc(HEAP_SIZE, sizeof(uint8_t));
    heap_ref = 0;
}

void mark_all_children(skiff_any_ref_t * data, skiff_any_ref_class_t * cls) 
{
    printf("Item is of class %s\n", cls->simple_name);

    if(data->mark == MARKED)
    {
        return;
    }

    size_t own_field_start = sizeof(void *);
    if(cls->parent != NULL)
    {
        printf("cls=%p\n", cls->parent);
        own_field_start = ((skiff_any_ref_class_t *) cls->parent)->struct_size;
        mark_all_children(data, cls->parent);
    }

    uint8_t * raw_item = (uint8_t *) data;
    printf("data start: %li\n", own_field_start);
    for (size_t i = 0; i < cls->class_refs; i++)
    {
        printf("i: %li\n", i);
        uint8_t * child_offset = raw_item + own_field_start + i;
        skiff_any_ref_t ** child = (skiff_any_ref_t **) (child_offset);
        mark_all_children(*child, (*child)->class_ptr);
    }

    if(data->class_ptr == (skiff_any_ref_class_t *) &skiff_list_interface)
    {
        skiff_list_t * lst = (skiff_list_t *) data;
        for (size_t j = 0; j < lst->size; j++)
        {
            skiff_any_ref_t * list_item = lst->data[j];
            mark_all_children(list_item, list_item->class_ptr);
        }
    }
    
    data->mark = MARKED;
    printf("Marked\n");
}

size_t skiff_size(skiff_any_ref_t * item)
{
    size_t size = item->class_ptr->struct_size;
    if(item->class_ptr == (skiff_any_ref_class_t *) &skiff_string_interface)
    {
        size += sizeof(char) * (((skiff_string_t *) item)->len + 1);
    }
    if(item->class_ptr == (skiff_any_ref_class_t *) &skiff_list_interface)
    {
        size += sizeof(void *) * (((skiff_list_t *) item)->_cap);
    }
    return size;
}

void gc_stats()
{
    // The mark
    for (size_t j = 0; j < sp_ref; j++)
    {
        skiff_any_ref_t * item = skiff_ref_stack[j];
        if(item == 0)
        {
            continue;
        }
        printf("Item class=%s\n", item->class_ptr->simple_name);
        skiff_any_ref_class_t * cls = (skiff_any_ref_class_t *) item->class_ptr;
        mark_all_children(item, cls);
    }

    // The sweep
    uint8_t * raw_item = heap_space;
    uint8_t * last_open = NULL;
    while(raw_item < heap_space + heap_ref) 
    {
        skiff_any_ref_t * item = raw_item;
        if(item->mark == CLEAN)
        {
            printf("Found garbage! cls=%s\n", item->class_ptr->simple_name);
            last_open = raw_item;
        }
        else
        {
            item->mark = CLEAN;
            // if(last_open != NULL)
            // {
            //     memcpy(last_open, raw_item, skiff_size(item));
            // }
        }
        raw_item += skiff_size(item);
    }
}