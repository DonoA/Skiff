#pragma once

void * skalloc(size_t, size_t);

typedef struct skiff_any_ref_struct skiff_any_ref_t;
typedef struct skiff_any_ref_class_struct skiff_any_ref_class_t;
struct skiff_any_ref_class_struct 
{
    int32_t class_refs;
    int32_t struct_size;
    void * parent;
    char * simple_name;
};
struct skiff_any_ref_class_struct skiff_any_ref_interface;
struct skiff_any_ref_struct 
{
    skiff_any_ref_class_t * class_ptr;
    uint8_t mark;
};

void skiff_any_ref_static()
{
    skiff_any_ref_interface.class_refs = 0;
    skiff_any_ref_interface.struct_size = sizeof(skiff_any_ref_t);
    skiff_any_ref_interface.parent = NULL;
    skiff_any_ref_interface.simple_name = "AnyRef";
}