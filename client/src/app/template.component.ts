/* tslint:disable:directive-selector */

import { Directive, NgModule, OnChanges, Component, Input, ViewContainerRef, Compiler, ComponentRef } from '@angular/core';
import { Http, Response } from '@angular/http';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';
import { Logger } from './logger.service';

@Component({
    selector: 'k-template',
    template: ''
})
export class TemplateComponent implements OnChanges {

    @Input() url: string;
    @Input() data: any;

    constructor(private http: Http, private vcRef: ViewContainerRef, private compiler: Compiler, private log: Logger) { }

    private createComponent(template: string, data) {

        @Component({
            selector: 'k-dynamic-template',
            template: template
        })
        class DynamicTemplateComponent {
            page = {};
        };

        @NgModule({
            imports: [CommonModule],
            declarations: [DynamicTemplateComponent],
            //declarations: [ArticleComponent, DynamicTemplateComponent],
            //exports: [ArticleComponent]
        })
        class DynamicTemplateModule { }

        this.compiler.compileModuleAndAllComponentsAsync(DynamicTemplateModule)
            .then(factory => {
                const compFactory = factory.componentFactories.find(x => x.componentType === DynamicTemplateComponent);
                const cmpRef: ComponentRef<DynamicTemplateComponent> = this.vcRef.createComponent(compFactory, 0);
                cmpRef.instance.page = data;
            });
    }

    ngOnChanges() {
        if (!this.url || !this.data) {
            this.log.warn('Didn\'t find template');
            return;
        };
        this.http.get(this.url)
            .toPromise()
            .then((response: Response) => {
                this.log.info('Template: ' + this.url);
                const template = response.text();
                this.log.debug('Template data: ' + template);
                this.createComponent(template, this.data);
            }).catch((error) => {
                this.log.error('Error getting template: ' + error);
            });
    }
}
