#pragma once

#include <stdlib.h>
#include <stdio.h>
#include <stdbool.h>
#include "skiff_anyref.h"
#include "skiff_string.h"

#define STACK_SIZE 4096
#define HEAP_SIZE 4096
#define REF_SIZE 8

#define CLEAN 0
#define MARKED 1

skiff_any_ref_t * skiff_ref_stack[STACK_SIZE/REF_SIZE] = { 0 };
uint8_t * eden_space;
uint8_t * survivor_space;
uint8_t * old_gen_space;

size_t sp_ref = 0;
size_t eden_ref = 0;
size_t survivor_ref = 0;
size_t old_gen_ref = 0;

void run_gc();

void * skalloc(size_t i, size_t size)
{
    printf("Alloc %li bytes\n", size);
    if(eden_ref + size > HEAP_SIZE)
    {
        printf("Out of eden space, running GC\n");
        run_gc();
    }
    uint8_t * data = eden_space + eden_ref;
    eden_ref += size;
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
    eden_space = calloc(HEAP_SIZE, sizeof(uint8_t));
    survivor_space = calloc(HEAP_SIZE, sizeof(uint8_t));
    old_gen_space = calloc(HEAP_SIZE, sizeof(uint8_t));
    
    eden_ref = 0;
    survivor_ref = 0;
    old_gen_ref = 0;
}

void mark_all_children(skiff_any_ref_t * data, skiff_any_ref_class_t * cls) 
{
    printf("Marking a %s\n", cls->simple_name);

    if(data->mark == MARKED)
    {
        return;
    }

    size_t own_field_start = sizeof(void *);
    if(cls->parent != NULL)
    {
        // printf("cls=%p\n", cls->parent);
        own_field_start = ((skiff_any_ref_class_t *) cls->parent)->struct_size;
        mark_all_children(data, cls->parent);
    }

    uint8_t * raw_item = (uint8_t *) data;
    // printf("data start: %li\n", own_field_start);
    for (size_t i = 0; i < cls->class_refs; i++)
    {
        // printf("i: %li\n", i);
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
    // printf("Marked\n");
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

void print_space(char * name, uint8_t * space, size_t ref)
{
    printf("%s:\n", name);
    uint8_t * raw_item = space;
    while(raw_item < space + ref)
    {
        skiff_any_ref_t * item = (skiff_any_ref_t *) raw_item;
        size_t item_size = skiff_size(item);
        printf("%s Object (%li bytes)\n", item->class_ptr->simple_name, item_size);
        raw_item += item_size;
    }
}

void copy_heap_data(uint8_t * src_obj, size_t size, uint8_t * dest_heap, 
                    size_t dest_start)
{
    for (size_t j = 0; j < size; j++)
    {
        dest_heap[dest_start + j] = src_obj[j];
    }
}

bool in_space(void * p, uint8_t * space, size_t space_ref)
{
    return ((void *) space < p && p < (void *) (space + space_ref));
}

void rewrite_refs(skiff_any_ref_t * data, skiff_any_ref_class_t * cls)
{
    printf("Rewriting refs for %s\n", cls->simple_name);

    if(data->mark == MARKED)
    {
        return;
    }

    size_t own_field_start = sizeof(void *);
    if(cls->parent != NULL)
    {
        // printf("cls=%p\n", cls->parent);
        own_field_start = ((skiff_any_ref_class_t *) cls->parent)->struct_size;
        rewrite_refs(data, cls->parent);
    }

    uint8_t * raw_item = (uint8_t *) data;
    // printf("data start: %li\n", own_field_start);
    for (size_t i = 0; i < cls->class_refs; i++)
    {
        // printf("i: %li\n", i);
        uint8_t * child_offset = raw_item + own_field_start + i;
        skiff_any_ref_t ** child = (skiff_any_ref_t **) (child_offset);
        if(!in_space(*child, eden_space, eden_ref))
        {
            continue;
        }
        // make this field point to it's new location which should be recorded 
        // at the start of where the object used to be
        *child = *((void **) *child);
    }

    if(data->class_ptr == (skiff_any_ref_class_t *) &skiff_list_interface)
    {
        skiff_list_t * lst = (skiff_list_t *) data;
        for (size_t j = 0; j < lst->size; j++)
        {
            skiff_any_ref_t ** list_item = lst->data + j;
            if(!in_space(*list_item, eden_space, eden_ref))
            {
                continue;
            }

            *list_item = *((void **) *list_item);
        }
    }

    printf("Done Rewriting refs for %s\n", cls->simple_name);
}

void run_gc()
{
    print_space("Eden", eden_space, eden_ref);

    // The mark
    for (size_t j = 0; j < sp_ref; j++)
    {
        skiff_any_ref_t * item = skiff_ref_stack[j];
        if(item == 0)
        {
            continue;
        }
        printf("Stack item for mark class = %s\n", item->class_ptr->simple_name);
        skiff_any_ref_class_t * cls = (skiff_any_ref_class_t *) item->class_ptr;
        mark_all_children(item, cls);
    }

    size_t garbage_size = 0;

    // The sweep (for eden space)
    uint8_t * raw_item = eden_space;
    while(raw_item < eden_space + eden_ref) 
    {
        skiff_any_ref_t * item = (skiff_any_ref_t *) raw_item;
        size_t item_size = skiff_size(item);
        if(item->mark == CLEAN)
        {
            printf("Found garbage! cls = %s\n", item->class_ptr->simple_name);
            garbage_size += item_size;
        }
        else
        {
            // copy in this non garbage to survivor space
            item->mark = CLEAN;

            // copy item to survivor space
            printf("Copy up a %s (%li bytes)\n", item->class_ptr->simple_name, item_size);
            copy_heap_data(raw_item, item_size, survivor_space, survivor_ref); 

            void ** item_p = (void **) raw_item;
            *item_p = survivor_space + survivor_ref; // drop a ref
            
            survivor_ref += item_size; // update survivor heap usage
        }
        raw_item += item_size;
    }

    print_space("Survivor", survivor_space, survivor_ref);

    // repoint things that point to eden space so that they point to new locations
    raw_item = survivor_space;
    while(raw_item < survivor_space + survivor_ref) 
    {
        skiff_any_ref_t * item = (skiff_any_ref_t *) raw_item;
        size_t item_size = skiff_size(item);

        rewrite_refs(item, item->class_ptr);

        raw_item += item_size;
    }

    for (size_t j = 0; j < sp_ref; j++)
    {
        skiff_any_ref_t ** item = skiff_ref_stack + j;
        if(item == 0)
        {
            continue;
        }
        *item = *((void **) *item);
    }

    printf("gc done, reclaimed %li bytes\n", garbage_size);

    // delete eden space
    eden_ref = 0;
}