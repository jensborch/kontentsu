import { NgModule, OnChanges, Component, Input, ViewContainerRef, AfterViewInit, Compiler, ComponentRef, ViewChild } from '@angular/core';
import { Http, Response } from '@angular/http';
import { CommonModule } from '@angular/common';
import { PageModule } from './page.module';
import { ArticleComponent } from './article/article.component';
import { Logger } from './logger.service';


@Component({
    selector: 'k-template',
    template: '<ng-template #target></ng-template>'
})
export class TemplateComponent implements OnChanges, AfterViewInit {

    @ViewChild('target', { read: ViewContainerRef }) target: ViewContainerRef;
    @Input() url: string;
    @Input() data: any;
    private initialized = false;

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
            imports: [CommonModule, PageModule],
            declarations: [DynamicTemplateComponent],
            exports: [PageModule]
        })
        class DynamicTemplateModule { }

        this.compiler.compileModuleAndAllComponentsAsync(DynamicTemplateModule)
            .then(factory => {
                const compFactory = factory.componentFactories.find(x => x.componentType === DynamicTemplateComponent);
                this.target.clear();
                const cmpRef: ComponentRef<DynamicTemplateComponent> = this.target
                    .createComponent(compFactory);
                cmpRef.instance.page = data;
            });
    }

    private loadTemplate() {
        if (!this.url || !this.data) {
            this.log.warn('Didn\'t find template');
            return;
        };
        if (!this.initialized) {
            return;
        }
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

    ngOnChanges() {
        this.loadTemplate();
    }

    ngAfterViewInit() {
        this.initialized = true;
        this.loadTemplate();
    }

}
