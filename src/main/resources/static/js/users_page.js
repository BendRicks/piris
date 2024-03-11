window.onload = async () => {

    await loadUserInfo()
    await getAccounts()
    await getObligations()

    document.getElementById("modal-header-cross").addEventListener('click', () => {
        document.getElementById("modal").style.display = 'none'
        // document.getElementById('modal-content').innerHTML = ''
    })

    document.getElementById("modal-bg").addEventListener('click', () => {
        document.getElementById("modal").style.display = 'none'
        // document.getElementById('modal-content').innerHTML = ''
    })

    restoreTab()

}

async function loadUserInfo() {
    let response = await fetch(`/auth/me`, {
        method: 'GET',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        let user = await response.json()
        document.getElementById('Personal').innerHTML =
            `
                    <div class="personal-info">
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
                    `
    }
}

async function getObligations() {
    let response = await fetch(`/obligations/my`, {
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
                                ${depositObl.mainAccount.status == 'END_OF_SERVICE' ? '<button onclick="openTransactionModal(' + depositObl.mainAccount.iban + ')">Перевести деньги</button>' : ''}
                            </div>
                        </div>
                        <div class="account-item">
                            <p>Имя: ${depositObl.percentAccount.name}</p>
                            <p>Тип: ${depositObl.percentAccount.accountType.description}</p>
                            <p>Баланс: ${depositObl.percentAccount.balance / 100} ${depositObl.percentAccount.currency}</p>
                        </div>
                        <div class="panel">
                            <div class="panel"><div class="panel-first-line">
                                <p>Статус: ${recordStatusEnum[depositObl.percentAccount.status]}</p>
                                <p>IBAN: ${depositObl.percentAccount.iban}</p>
                                <button onclick="openTransactionModal('${depositObl.percentAccount.iban}')">Перевести деньги</button>
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
                        <p>Депозитный план: ${creditObl.obligationPlan.name} ${creditObl.obligationPlan.planPercent}% ${creditObl.obligationPlan.currency} до ${creditObl.obligationPlan.months} месяцев</p>
                        <p>Сумма вклада: ${creditObl.amount / 100} ${creditObl.currency}</p>
                        <div class="account-item">
                            <p>Имя: ${creditObl.mainAccount.name}</p>
                            <p>Тип: ${creditObl.mainAccount.accountType.description}</p>
                            <p>Баланс: ${creditObl.mainAccount.balance / 100} ${creditObl.mainAccount.currency}</p>
                        </div>
                        <div class="panel">
                            <div class="panel-first-line">
                                <p>Статус: ${recordStatusEnum[creditObl.mainAccount.status]}</p>
                                <p>IBAN: ${creditObl.mainAccount.iban}</p>
                                <button onclick="openTransactionModal('${creditObl.mainAccount.iban}')">Перевести деньги</button>
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
        document.getElementById('Obligations').innerHTML = innerHtml
        await configureAccordeons()
    }
}

async function getAccounts() {
    let response = await fetch(`/accounts/my`, {
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
                        <p class="panel-card-item">Карта ${card.number} <button onclick="updateCardCredentials('${card.number}')">Обновить CVC/CCV,PIN</button></p>
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
                            <button onclick="openTransactionModal('${paymAcc.iban}')">Перевести деньги</button>
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
                            ${depositAcc.accountType.code != 3404 || (depositAcc.accountType.code == 3404 && depositAcc.status == 'END_OF_SERVICE') ? '<button onclick="openTransactionModal(\'' + depositAcc.iban + '\')">Перевести деньги</button>' : ''}
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
                        <p class="panel-card-item">Карта ${card.number} <button onclick="updateCardCredentials('${card.number}')">Обновить CVC/CCV,PIN</button></p>
                    `
                }
            }
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
                            ${creditAcc.accountType.code != 2470 ? '<button onclick="openTransactionModal(\'' + creditAcc.iban + '\')">Перевести деньги</button>' : ''}
                        </div>
                        ${cardsInsertion}
                    </div>
                    `
        }
        await configureAccordeons()
        document.getElementById('Accounts').innerHTML = innerHtml
    }
}

async function updateCardCredentials(cardNo) {
    let response = await fetch(`/accounts/card/${cardNo}/update_credentials`, {
        method: 'POST',
        headers: {
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    let respData = await response.json();
    alert(respData.message);
}

async function transferMoney(ev) {
    ev.preventDefault()
    let formData = new FormData(document.getElementById('transactionForm'))
    let transactionData = {
        sender: {
            iban: formData.get('senderAcc')
        },
        recipient: {
            iban: formData.get('recipientAcc')
        },
        amount: formData.get('amount') * 100
    }
    let response = await fetch(`/accounts/transaction/create`, {
        method: 'POST',
        body: JSON.stringify(transactionData),
        headers: {
            'content-type': 'application/json',
            'X-CSRF-TOKEN': _csrfToken
        }
    })
    if (response.status == 200) {
        alert("Транзакция прошла успешно")
        location.reload()
        // document.getElementById("modal").style.display = 'none'
    } else {
        let error = await response.json();
        alert(error.message);
    }
}

async function openTransactionModal(id) {
    let modal = document.getElementById('modal')
    document.getElementById('senderAcc').value = id
    modal.style.display = 'block'
}
