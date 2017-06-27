/* tslint:disable:no-unused-variable */

import { HttpModule, XHRBackend, ResponseOptions } from '@angular/http';
import { Title } from '@angular/platform-browser';
import { TestBed, async, inject } from '@angular/core/testing';
import { MockBackend } from '@angular/http/testing';
import { ContentService } from './content.service';
import { Logger } from './logger.service';

const mockResponse = {
  content: {
    heading: 'Testing'
  }
};

describe('Service: Content', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpModule],
      providers: [
        { provide: XHRBackend, useClass: MockBackend },
        { provide: Title, useClass: Title },
        ContentService,
        Logger
      ]
    });
  });

  it('should create the component', inject([ContentService], (service: ContentService) => {
    expect(service).toBeTruthy();
  }));

  it('should return a page', inject([ContentService, XHRBackend], (service: ContentService, mockBackend: MockBackend) => {
    mockBackend.connections.subscribe((connection) => {
      connection.mockRespond(new Response(new ResponseOptions({
        body: JSON.stringify(mockResponse)
      })));
    });
    service.getPage().subscribe(page => {
      expect(page).toBeDefined();
      expect(page.data).toBeDefined();
      expect(page.data.heading).toEqual('Testing');
    });
  }));

  it('should set title', inject([ContentService, XHRBackend, Title], (service: ContentService, mockBackend: MockBackend, title: Title) => {
    mockBackend.connections.subscribe((connection) => {
      connection.mockRespond(new Response(new ResponseOptions({
        body: JSON.stringify(mockResponse)
      })));
    });
    service.getPage().subscribe(page => {
      expect(title.getTitle()).toEqual('Test title');
    });
  }
  ));
});
