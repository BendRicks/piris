window.onload = async () => {

    document.getElementById("modal-header-cross").addEventListener('click', () => {
        document.getElementById("modal").style.display = 'none'
    })

    document.getElementById("user-modal-header-cross").addEventListener('click', () => {
        document.getElementById("user-modal").style.display = 'none'
    })

    document.getElementById("account-modal-header-cross").addEventListener('click', () => {
        document.getElementById("account-modal").style.display = 'none'
    })

    document.getElementById("obligation-modal-header-cross").addEventListener('click', () => {
        document.getElementById("obligation-modal").style.display = 'none'
    })

    document.getElementById("modal-bg").addEventListener('click', () => {
        document.getElementById("modal").style.display = 'none'
    })

    document.getElementById("user-modal-bg").addEventListener('click', () => {
        document.getElementById("user-modal").style.display = 'none'
    })

    document.getElementById("account-modal-bg").addEventListener('click', () => {
        document.getElementById("account-modal").style.display = 'none'
    })

    document.getElementById("obligation-modal-bg").addEventListener('click', () => {
        document.getElementById("obligation-modal").style.display = 'none'
    })

    await getAllUsers();
}

async function deleteUser(id) {
    let response = await fetch(`/users/${id}`, {
        method: 'DELETE',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        document.getElementById(`user_${id}`).remove()
    }
    document.getElementById("modal").style.display = 'none'
    document.getElementById('modal-content').innerHTML = ''
}

async function openUserModal(id) {
    let user = await getUser(id)
    if (user) {
        let modal = document.getElementById('modal')
        let container = document.getElementById('adminPersonal')
        container.innerHTML = `
                        <div class="admin-personal-info">
                            <p>ФИО: ${user.surname} ${user.name} ${user.givenName}</p>
                            <p>Дата рождения: ${user.birthDate}</p>
                            <p>Пол: ${sexEnum[user.sex]}</p>
                            <p>Идентификационный номер: ${user.passportId}</p>
                            <p>Орган, выдавший документ: ${user.competentOrgan}</p>
                            <p>Дата выдачи: ${user.dateOfIssue}</p>
                            <p>Номер паспорта: ${user.passportSerial}</p>
                            <p>Город проживания: ${cityOfResidenceEnum[user.cityOfResidence]}</p>
                            <p>Место рождения: ${user.birthLocation}</p>
                            <p>Адрес проживания: ${user.addressOfLiving}</p>
                            <p>Адрес прописки: ${user.placeOfResidence}</p>
                            <p>Домашний телефон: ${user.homePhoneNumber}</p>
                            <p>Мобильный телефон: ${user.mobilePhoneNumber}</p>
                            <p>email: ${user.email}</p>
                            <p>Гражданство: ${citizenshipEnum[user.citizenship]}</p>
                            <p>Семейное положение: ${maritalStatusEnum[user.maritalStatus]}</p>
                            <p>Инвалидность: ${disabilityEnum[user.disability]}</p>
                            <p>Пенсионер: ${user.pensioner === "true" ? "Да" : "Нет"}</p>
                            <p>Ежемесячный доход: ${user.monthlyIncome}</p>
                        </div>
                        <div class="modal-window-content-row">
                            <button value="Удалить" onclick="deleteUser(${user.id})">Удалить</button>
                            <button value="Изменить" onclick="updateUserModel(${user.id})">Изменить</button>
                        </div>
                        `
        await getAccountsByUserId(id)
        await getObligationsByUserId(id)
        modal.style.display = 'block'
        await configureAccordeons()
    } else {
        alert("Произошла ошика при загрузке пользователей")
    }
}

async function openAccountModal(id) {
    let modal = document.getElementById('account-modal')
    document.getElementById('owner.id').value = id
    modal.style.display = 'block'
}

async function openObligationModal(id) {
    let modal = document.getElementById('obligation-modal')
    document.getElementById('obligation.owner.id').value = id
    let response = await fetch(`/accounts/user/${id}/payment?currency=BYN`, {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    let data = await response.json()
    let innerHtml = ''
    for (const el of data) {
        innerHtml += `
                <option value="${el.iban}">${el.iban} ${el.balance / 100} ${el.currency}</option>
                `
    }
    document.getElementById('paymentIban').innerHTML = innerHtml
    modal.style.display = 'block'
}

async function createNewAccount(ev) {
    ev.preventDefault()
    let formData = new FormData(document.getElementById('create-account-form'))
    let data = {
        owner: {
            id: formData.get('owner.id')
        },
        name: formData.get('account-name'),
        currency: formData.get('currency')
    }
    console.log(JSON.stringify(data))
    let response = await fetch(`/accounts/payment/create`, {
        method: 'POST',
        body: JSON.stringify(data),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        alert("Создание счёта прошло успешно")
        location.reload()
        // document.getElementById("account-modal").style.display = 'none'
    } else {
        let error = await response.json();
        alert(JSON.stringify(error.errors));
    }
}

async function createNewObligation(ev) {
    ev.preventDefault()
    let formData = new FormData(document.getElementById('create-obligation-form'))
    let data = {
        obligation: {
            owner: {
                id: formData.get('obligation.owner.id')
            },
            obligationPlan: {
                id: formData.get('obligation.obligationPlan.id')
            },
            startTime: formData.get('obligation.startTime')
        },
        startBalance: formData.get('startBalance') * 100,
        months: formData.get('months'),
        paymentIban: formData.get('paymentIban')
    }
    let response = await fetch(`/obligations/create`, {
        method: 'POST',
        body: JSON.stringify(data),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        alert("Создание договора прошло успешно")
        location.reload()
        // document.getElementById("obligation-modal").style.display = 'none'
    } else {
        let error = await response.json();
        alert(JSON.stringify(error.errors));
    }
}

async function getAccountsByUserId(id) {
    let response = await fetch(`/accounts/user/${id}/all`, {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        let accounts = await response.json()
        let innerHtml = `
                <h2>Платёжные счета:</h2>
                <hr>
                `
        for (const paymAcc of accounts.payment) {
            let cardsInsertion = ''
            if (paymAcc.cards) {
                for (const card of paymAcc.cards) {
                    cardsInsertion += `
                        <p class="panel-card-item">Карта ${card.number}</p>
                    `
                }
            }
            innerHtml += `
                    <div class="account-item">
                        <p>Имя: ${paymAcc.name}</p>
                        <p>Тип: ${paymAcc.accountType.description}</p>
                        <p>Баланс: ${paymAcc.balance / 100} ${paymAcc.currency}</p>
                    </div>
                    <div class="panel">
                        <div class="panel-first-line">
                            <p>Статус: ${recordStatusEnum[paymAcc.status]}</p>
                            <p>IBAN: ${paymAcc.iban}</p>
                            <button onclick="createCard('${paymAcc.iban}')">Создать виртуальную карточку</button>
                        </div>
                        ${cardsInsertion}
                    </div>
                    `
        }
        innerHtml += `
                <h2>Депозтные счета:</h2>
                <hr>
                `
        for (const depositAcc of accounts.deposit) {
            innerHtml += `
                    <div class="account-item">
                        <p>Имя: ${depositAcc.name}</p>
                        <p>Тип: ${depositAcc.accountType.description}</p>
                        <p>Баланс: ${depositAcc.balance / 100} ${depositAcc.currency}</p>
                    </div>
                    <div class="panel">
                        <div class="panel-first-line">
                            <p>Статус: ${recordStatusEnum[depositAcc.status]}</p>
                            <p>IBAN: ${depositAcc.iban}</p>
                        </div>
                    </div>
                    `
        }
        innerHtml += `
                <h2>Кредитные счета:</h2>
                <hr>
                `
        for (const creditAcc of accounts.credit) {
            let cardsInsertion = ''
            if (creditAcc.cards) {
                for (const card of creditAcc.cards) {
                    cardsInsertion += `
                        <p class="panel-card-item">Карта ${card.number}</p>
                    `
                }
            }
            let createCard = creditAcc.accountType.code == 2400 && creditAcc.status == 'ACTIVE' ? `<button onclick="createCard('${creditAcc.iban}')">Создать виртуальную карточку</button>` : ''
            innerHtml += `
                    <div class="account-item">
                        <p>Имя: ${creditAcc.name}</p>
                        <p>Тип: ${creditAcc.accountType.description}</p>
                        <p>Баланс: ${creditAcc.balance / 100} ${creditAcc.currency}</p>
                    </div>
                    <div class="panel">
                        <div class="panel-first-line">
                            <p>Статус: ${recordStatusEnum[creditAcc.status]}</p>
                            <p>IBAN: ${creditAcc.iban}</p>
                            ${createCard}
                        </div>
                        ${cardsInsertion}
                    </div>
                    `
        }
        innerHtml += `<button value="Создать" onclick="openAccountModal(${id})">Создать счет</button>`
        document.getElementById('adminAccounts').innerHTML = innerHtml
    }
}

async function getObligationsByUserId(id) {
    let response = await fetch(`/obligations/user/${id}/all`, {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        let obligations = await response.json()
        let innerHtml = `
                <h2>Депозитные договора:</h2>
                <hr>
                `
        for (const depositObl of obligations.deposit) {
            innerHtml += `
                    <div class="account-item">
                        <p>Номер договора: ${depositObl.contractNumber}</p>
                        <p>Тип договора: ${obligationTypeEnum[depositObl.obligationType]}</p>
                        <p>Валюта: ${depositObl.currency}</p>
                    </div>
                    <div class="panel">
                        <p>Депозитный план: ${depositObl.obligationPlan.name} ${depositObl.obligationPlan.planPercent}% ${depositObl.obligationPlan.currency} до ${depositObl.obligationPlan.months} месяцев</p>
                        <p>Сумма вклада: ${depositObl.amount / 100} ${depositObl.currency}</p>
                        <div class="account-item">
                            <p>Имя: ${depositObl.mainAccount.name}</p>
                            <p>Тип: ${depositObl.mainAccount.accountType.description}</p>
                            <p>Баланс: ${depositObl.mainAccount.balance / 100} ${depositObl.mainAccount.currency}</p>
                        </div>
                        <div class="panel">
                            <div class="panel-first-line">
                                <p>Статус: ${recordStatusEnum[depositObl.mainAccount.status]}</p>
                                <p>IBAN: ${depositObl.mainAccount.iban}</p>
                            </div>
                        </div>
                        <div class="account-item">
                            <p>Имя: ${depositObl.percentAccount.name}</p>
                            <p>Тип: ${depositObl.percentAccount.accountType.description}</p>
                            <p>Баланс: ${depositObl.percentAccount.balance / 100} ${depositObl.percentAccount.currency}</p>
                        </div>
                            <div class="panel"><div class="panel-first-line">
                                <p>Статус: ${recordStatusEnum[depositObl.percentAccount.status]}</p>
                                <p>IBAN: ${depositObl.percentAccount.iban}</p>
                            </div>
                        </div>
                    </div>
                    `
        }
        innerHtml += `
                <h2>Кредитные договоры:</h2>
                <hr>
                `
        for (const creditObl of obligations.credit) {
            let cardsInsertion = ''
            if (creditObl.mainAccount.cards) {
                for (const card of creditObl.mainAccount.cards) {
                    cardsInsertion += `
                        <p class="panel-card-item">Карта ${card.number}</p>
                    `
                }
            }
            innerHtml += `
                    <div class="account-item">
                        <p>Номер договора: ${creditObl.contractNumber}</p>
                        <p>Тип договора: ${obligationTypeEnum[creditObl.obligationType]}</p>
                        <p>Валюта: ${creditObl.currency}</p>
                    </div>
                    <div class="panel">
                        <p>Кредитный план: ${creditObl.obligationPlan.name} ${creditObl.obligationPlan.planPercent}% ${creditObl.obligationPlan.currency} до ${creditObl.obligationPlan.months} месяцев</p>
                        <p>Сумма кредита: ${creditObl.amount / 100} ${creditObl.currency}</p>
                        <div class="account-item">
                            <p>Имя: ${creditObl.mainAccount.name}</p>
                            <p>Тип: ${creditObl.mainAccount.accountType.description}</p>
                            <p>Баланс: ${creditObl.mainAccount.balance / 100} ${creditObl.mainAccount.currency}</p>
                        </div>
                        <div class="panel">
                            <div class="panel-first-line">
                                <p>Статус: ${recordStatusEnum[creditObl.mainAccount.status]}</p>
                                <p>IBAN: ${creditObl.mainAccount.iban}</p>
                                ${creditObl.mainAccount.status == 'ACTIVE' ? '<button onclick="createCard(' + creditObl.mainAccount.iban + ')">Создать виртуальную карточку</button>`' : ''}
                            </div>
                            ${cardsInsertion}
                        </div>
                        <div class="account-item">
                            <p>Имя: ${creditObl.percentAccount.name}</p>
                            <p>Тип: ${creditObl.percentAccount.accountType.description}</p>
                            <p>Баланс: ${creditObl.percentAccount.balance / 100} ${creditObl.percentAccount.currency}</p>
                        </div>
                        <div class="panel">
                            <div class="panel-first-line">
                                <p>Статус: ${recordStatusEnum[creditObl.percentAccount.status]}</p>
                                <p>IBAN: ${creditObl.percentAccount.iban}</p>
                            </div>
                        </div>
                    </div>
                    `
        }
        innerHtml += `<button value="Создать" onclick="openObligationModal(${id})">Создать договор</button>`
        document.getElementById('adminObligations').innerHTML = innerHtml
    }
}

async function createUserModel() {
    document.getElementById('id').value = '';
    document.getElementById('email').value = '';
    document.getElementById('surname').value = '';
    document.getElementById('name').value = '';
    document.getElementById('givenName').value = '';
    document.getElementById('birthDate').value = '';
    document.getElementById('sex').value = '';
    document.getElementById('passportSerial').value = '';
    document.getElementById('competentOrgan').value = '';
    document.getElementById('dateOfIssue').value = '';
    document.getElementById('passportId').value = '';
    document.getElementById('birthLocation').value = '';
    document.getElementById('cityOfResidence').value = '';
    document.getElementById('addressOfLiving').value = '';
    document.getElementById('homePhoneNumber').value = '';
    document.getElementById('mobilePhoneNumber').value = '';
    document.getElementById('placeOfResidence').value = '';
    document.getElementById('maritalStatus').value = '';
    document.getElementById('citizenship').value = '';
    document.getElementById('disability').value = '';
    document.getElementById('pensioner').value = '';
    document.getElementById('monthlyIncome').value = '';
    document.getElementById('user-create-button').onclick = (event) => {
        createNewUser(event);
        console.log('aboba');
    };
    await userModel({});
}

async function updateUserModel(id) {
    let user = await getUser(id);
    document.getElementById('id').value = user.id;
    document.getElementById('email').value = user.email;
    document.getElementById('surname').value = user.surname;
    document.getElementById('name').value = user.name;
    document.getElementById('givenName').value = user.givenName;
    document.getElementById('birthDate').value = user.birthDate;
    document.getElementById('sex').value = user.sex;
    document.getElementById('passportSerial').value = user.passportSerial;
    document.getElementById('competentOrgan').value = user.competentOrgan;
    document.getElementById('dateOfIssue').value = user.dateOfIssue;
    document.getElementById('passportId').value = user.passportId;
    document.getElementById('birthLocation').value = user.birthLocation;
    document.getElementById('cityOfResidence').value = user.cityOfResidence;
    document.getElementById('addressOfLiving').value = user.addressOfLiving;
    document.getElementById('homePhoneNumber').value = user.homePhoneNumber;
    document.getElementById('mobilePhoneNumber').value = user.mobilePhoneNumber;
    document.getElementById('placeOfResidence').value = user.placeOfResidence;
    document.getElementById('maritalStatus').value = user.maritalStatus;
    document.getElementById('citizenship').value = user.citizenship;
    document.getElementById('disability').value = user.disability;
    document.getElementById('pensioner').value = user.pensioner;
    document.getElementById('monthlyIncome').value = user.monthlyIncome;
    document.getElementById('user-create-button').onclick = (event) => {
        updateUser(event);
        console.log('aboba edit');
    };
    await userModel(user);
}

async function userModel(user) {
    if (user) {
        let modal = document.getElementById('user-modal')
        let container = document.getElementById('adminPersonal')
        container.innerHTML = `
                        `
        modal.style.display = 'block'
    } else {
        alert("Произошла ошика при загрузке пользователей")
    }
}

async function getUser(id) {
    let response = await fetch(`/users/user/${id}`, {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        return response.json();
    }
    return null;
}

async function createCard(iban) {
    let response = await fetch(`/accounts/${iban}/card/issue`, {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        alert("Карта создана успешно")
        location.reload()
    } else {
        let error = await response.json()
        alert(error.message)
    }
}

async function getAllUsers() {
    let response = await fetch('/users/get_all_users', {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        let container = document.getElementById('AllUsers')
        let innerHtml = '<h2>Пользователи</h2><hr>'
        let data = await response.json()
        for (const el of data) {
            innerHtml += `
                                <div class="user-item" id="user_${el.id}" onclick="openUserModal(${el.id})">
                                    <p>${el.surname} ${el.name} ${el.givenName}</p>
                                    <p>${el.passportId}</p>
                                </div>
                            `
        }
        innerHtml += `
                            <button value="Создать" onclick="createUserModel()">Создать пользователя</button>
                `
        container.innerHTML = innerHtml
        // console.log()
    } else {
        alert("Произошла ошика при загрузке пользователей")
    }
}

async function createNewUser(ev) {
    ev.preventDefault()
    let formData = new FormData(document.getElementById('create-user-form'))
    let userData = {
        email: formData.get('email'),
        surname: formData.get('surname'),
        name: formData.get('name'),
        givenName: formData.get('givenName'),
        birthDate: formData.get('birthDate'),
        sex: formData.get('sex'),
        passportSerial: formData.get('passportSerial'),
        competentOrgan: formData.get('competentOrgan'),
        dateOfIssue: formData.get('dateOfIssue'),
        passportId: formData.get('passportId'),
        birthLocation: formData.get('birthLocation'),
        cityOfResidence: formData.get('cityOfResidence'),
        addressOfLiving: formData.get('addressOfLiving'),
        homePhoneNumber: formData.get('homePhoneNumber'),
        mobilePhoneNumber: formData.get('mobilePhoneNumber'),
        placeOfResidence: formData.get('placeOfResidence'),
        maritalStatus: formData.get('maritalStatus'),
        citizenship: formData.get('citizenship'),
        disability: formData.get('disability'),
        pensioner: formData.get('pensioner'),
        monthlyIncome: formData.get('monthlyIncome'),
    }
    let response = await fetch(`/users/create`, {
        method: 'POST',
        body: JSON.stringify(userData),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        alert("Создание пользователя прошло успешно")
        location.reload()
        // document.getElementById("user-modal").style.display = 'none'
    } else {
        let error = await response.json();
        alert(JSON.stringify(error.errors));
    }
}

async function updateUser(ev) {
    ev.preventDefault()
    let formData = new FormData(document.getElementById('create-user-form'))
    let userData = {
        id: formData.get('id'),
        email: formData.get('email'),
        surname: formData.get('surname'),
        name: formData.get('name'),
        givenName: formData.get('givenName'),
        birthDate: formData.get('birthDate'),
        sex: formData.get('sex'),
        passportSerial: formData.get('passportSerial'),
        competentOrgan: formData.get('competentOrgan'),
        dateOfIssue: formData.get('dateOfIssue'),
        passportId: formData.get('passportId'),
        birthLocation: formData.get('birthLocation'),
        cityOfResidence: formData.get('cityOfResidence'),
        addressOfLiving: formData.get('addressOfLiving'),
        homePhoneNumber: formData.get('homePhoneNumber'),
        mobilePhoneNumber: formData.get('mobilePhoneNumber'),
        placeOfResidence: formData.get('placeOfResidence'),
        maritalStatus: formData.get('maritalStatus'),
        citizenship: formData.get('citizenship'),
        disability: formData.get('disability'),
        pensioner: formData.get('pensioner'),
        monthlyIncome: formData.get('monthlyIncome'),
    }
    let response = await fetch(`/users/edit`, {
        method: 'POST',
        body: JSON.stringify(userData),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        alert("Изменение пользователя прошло успешно")
        location.reload()
        // document.getElementById("user-modal").style.display = 'none'
    } else {
        let error = await response.json();
        alert(JSON.stringify(error.errors));
    }
}