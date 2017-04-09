import { Directive, NgModule, Component, Input, ViewContainerRef, Compiler, ComponentRef } from '@angular/core';

@Directive({
    selector: 'k-template'
})
export class TemplateComponent {

    @Input() url: string;
    @Input() data: any;


    constructor(private vcRef: ViewContainerRef, private compiler: Compiler) { }

    ngOnChanges() {
        const templateUrl = this.url;
        const data = this.data;
        if (!templateUrl || !data) return;

        @Component({
            selector: 'dynamic-comp',
            templateUrl: templateUrl
        })
        class DynamicTemplateComponent {
            page = {};
         };

        @NgModule({
            imports: [],
            declarations: [DynamicTemplateComponent]
        })
        class DynamicTemplateModule { }

        this.compiler.compileModuleAndAllComponentsAsync(DynamicTemplateModule)
            .then(factory => {
                const compFactory = factory.componentFactories.find(x => x.componentType === DynamicTemplateComponent);
                const cmpRef : ComponentRef<DynamicTemplateComponent> = this.vcRef.createComponent(compFactory, 0);
                cmpRef.instance.page = data;
            });
    }
}