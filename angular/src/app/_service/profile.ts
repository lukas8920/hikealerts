export class Profile {
  mail: string;
  organisation: string;
  status: string;
  api_key: string;

  constructor(mail: string, organisation: string, status: string, api_key: string) {
    this.mail = mail;
    this.organisation = organisation;
    this.status = status;
    this.api_key = api_key;
  }
}
