import { Directive, NgModule, Component, Input, ViewContainerRef, Compiler } from '@angular/core';

@Directive({
    selector: 'k-template'
})
export class TemplateComponent {

    @Input() url: string;

    constructor(private vcRef: ViewContainerRef, private compiler: Compiler) { }

    ngOnChanges() {
        const templateUrl = this.url;
        if (!templateUrl) return;

        @Component({
            selector: 'dynamic-comp',
            templateUrl: templateUrl
        })
        class DynamicTemplateComponent { };

        @NgModule({
            imports: [],
            declarations: [DynamicTemplateComponent]
        })
        class DynamicTemplateModule { }

        this.compiler.compileModuleAndAllComponentsAsync(DynamicTemplateModule)
            .then(factory => {
                const compFactory = factory.componentFactories.find(x => x.componentType === DynamicTemplateComponent);
                const cmpRef = this.vcRef.createComponent(compFactory, 0);
            });
    }
}